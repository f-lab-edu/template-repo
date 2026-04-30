package io.github.jaehyeonhan.project.service;

import static io.github.jaehyeonhan.project.service.ChatTestConst.ANOTHER_PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.ANOTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.BEGINNING_OF_TIME;
import static io.github.jaehyeonhan.project.service.ChatTestConst.BLOCK_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.MESSAGE_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.NON_EXISTENT_CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.THE_OTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.USER_ID;
import static io.github.jaehyeonhan.project.service.ChatTestConst.VALID_BLOCK_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.exception.AlreadyBlockedException;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.InvalidBlockDurationException;
import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import io.github.jaehyeonhan.project.exception.NotBlockedException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.exception.UnauthorizedBlockException;
import io.github.jaehyeonhan.project.exception.UnauthorizedSendMessageException;
import io.github.jaehyeonhan.project.exception.UnauthorizedUnblockException;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatValidationService chatValidationService;

    @Mock
    private ChatRepository chatRepository;

    @Mock
    private ParticipationRepository participationRepository;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private BlockRepository blockRepository;

    @Mock
    private IdGenerator idGenerator;

    @Spy
    private Clock clock = Clock.systemDefaultZone();

    @InjectMocks
    private ChatService chatService;

    /*
    테스트 메소드 명명 스타일
    given_조건_when_동작_then_결과
    */

    @Test
    @DisplayName("채팅 정상 생성 시 chat이 생성되고, 요청자가 참가자로 추가되며, chatId를 반환한다.")
    void given_title_when_create_then_createChatAndRequesterJoinsChatAndReturnsChatId() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "test");
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(idGenerator.generate()).willReturn(CHAT_ID, PARTICIPATION_ID);
        given(chatRepository.save(any(Chat.class))).willReturn(chat);
        given(participationRepository.save(any(Participation.class))).willReturn(participation);

        // when
        String returnedChatId = chatService.createChat(USER_ID, "test");

        // then
        assertThat(returnedChatId).isEqualTo(CHAT_ID);
        verify(chatRepository, times(1)).save(any(Chat.class));
        verify(participationRepository, times(1)).save(any(Participation.class));
    }

    @Test
    @DisplayName("빈 제목으로 채팅 생성 시 예외가 발생한다.")
    void given_emptyTitle_when_createChat_then_throwException() {
        // given
        given(idGenerator.generate()).willReturn(CHAT_ID);

        // when, then
        assertThatThrownBy(() -> chatService.createChat(USER_ID, ""))
            .isInstanceOf(InvalidChatTitleException.class);
    }

    @Test
    @DisplayName("채팅이 존재하고 처음 참가 시 채팅에 참가한다.")
    void given_chatExistsAndNotJoined_when_join_then_userJoinsChat() {
        // given
        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(participationRepository.existsByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(false);
        given(idGenerator.generate()).willReturn(PARTICIPATION_ID);
        given(participationRepository.save(any(Participation.class))).willReturn(participation);

        // when
        chatService.join(USER_ID, CHAT_ID);

        // then
        verify(participationRepository).save(any(Participation.class));
    }

    @Test
    @DisplayName("채팅에 중복으로 참여해도 참여 기록은 한 번만 생성된다.")
    void given_chatExists_when_joinParticipatingChat_then_oneParticipationIsCreated() {
        // given
        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(participationRepository.existsByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(false, true);
        given(idGenerator.generate()).willReturn(PARTICIPATION_ID);
        given(participationRepository.save(any(Participation.class))).willReturn(participation);

        // when
        chatService.join(USER_ID, CHAT_ID);
        chatService.join(USER_ID, CHAT_ID);

        // then
        verify(participationRepository, times(1)).save(any(Participation.class));
    }

    @Test
    @DisplayName("존재하지 않는 채팅에 참여할 시 예외가 발생한다.")
    void given_chatNotExists_when_join_then_throwException() {
        // given
        willThrow(new ChatNotFoundException("채팅이 없습니다."))
            .given(chatValidationService).requireChat(NON_EXISTENT_CHAT_ID);

        // when, then
        assertThatThrownBy(() -> chatService.join(USER_ID, NON_EXISTENT_CHAT_ID))
            .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("참여한 채팅에 메시지 전송 시 메시지가 저장된다.")
    void given_userJoinedChat_when_sendMessage_then_messageIsSaved() {
        // given
        Message message = new Message(MESSAGE_ID, CHAT_ID, USER_ID, "content", LocalDateTime.now());
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(USER_ID, CHAT_ID)).willReturn(participation);
        given(idGenerator.generate()).willReturn(MESSAGE_ID);
        given(messageRepository.save(any(Message.class))).willReturn(message);

        // when
        chatService.sendMessage(USER_ID, CHAT_ID, "content");

        // then
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    @DisplayName("참여하지 않은 채팅에 메시지 전송 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_sendMessage_then_throwException() {
        // given
        given(chatValidationService.requireParticipation(USER_ID, CHAT_ID))
            .willThrow(new NotParticipatingException("참여 중인 채팅이 아닙니다."));

        // when, then
        assertThatThrownBy(() -> chatService.sendMessage(USER_ID, CHAT_ID, "content"))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("참여한 채팅의 새 메시지 조회 시 메시지를 응답한다.")
    void given_userJoinedChat_when_getNewMessage_then_messageListIsReturned() {
        // given
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Message message = new Message(MESSAGE_ID, CHAT_ID, USER_ID, "content", LocalDateTime.now());

        given(chatValidationService.requireParticipation(USER_ID, CHAT_ID)).willReturn(participation);
        given(messageRepository.findMessagesAfterLastRead(CHAT_ID, BEGINNING_OF_TIME)).willReturn(List.of(message));

        // when
        List<MessageDto> newMessageList = chatService.getMessageList(USER_ID, CHAT_ID, BEGINNING_OF_TIME);

        // then
        assertThat(newMessageList).anyMatch(d -> d.getId().equals(MESSAGE_ID));
    }

    @Test
    @DisplayName("참여하지 않은 채팅의 새 메시지 조회 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_getNewMessage_then_throwException() {
        // given
        given(chatValidationService.requireParticipation(USER_ID, CHAT_ID))
            .willThrow(new NotParticipatingException("참여 중인 채팅이 아닙니다."));

        // when, then
        assertThatThrownBy(() -> chatService.getMessageList(USER_ID, CHAT_ID, BEGINNING_OF_TIME))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("방장과 관리자는 다른 일반 참가자의 메시지 전송을 차단할 수 있다.")
    void given_chatCreatorAndManager_when_blockUser_then_userIsBlocked() {
        // given
        ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation((USER_ID), (CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation((ANOTHER_USER_ID), (CHAT_ID))).willReturn(user);
        given(idGenerator.generate()).willReturn(BLOCK_ID);

        // when
        chatService.blockUser(creator.getUserId(), user.getUserId(), CHAT_ID, VALID_BLOCK_DURATION);

        // then
        then(blockRepository).should().save(captor.capture());
        assertThat(captor.getValue().getParticipationId()).isEqualTo(user.getId());
    }

    @Test
    @DisplayName("일반 참가자는 다른 참가자의 메시지 전송을 차단할 수 없다.")
    void given_user_when_blocksOthers_then_throwException() {
        // given
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(eq(USER_ID), eq(CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(user);

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(user.getUserId(), creator.getUserId(), CHAT_ID, VALID_BLOCK_DURATION);
        }).isInstanceOf(UnauthorizedBlockException.class);
    }

    @Test
    @DisplayName("관리자는 서로의 메시지 전송을 차단할 수 없다.")
    void given_chatManager_when_blockEachOther_then_throwException() {
        // given
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        String manager1ParticipationId = "619da13e-8564-43b1-9913-fbabfe6c61c1";
        Participation manager1 = getManager(creator, manager1ParticipationId, ANOTHER_USER_ID);
        String manager2ParticipationId = "449388e5-1544-48cc-91e0-8c2363713236";
        Participation manager2 = getManager(creator, manager2ParticipationId, THE_OTHER_USER_ID);

        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(manager1);
        given(chatValidationService.requireParticipation(eq(THE_OTHER_USER_ID), eq(CHAT_ID))).willReturn(manager2);

        // when, then
        assertThatThrownBy(
            () -> chatService.blockUser(manager1.getUserId(), manager2.getUserId(), CHAT_ID, VALID_BLOCK_DURATION))
            .isInstanceOf(UnauthorizedBlockException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 31, -1})
    @DisplayName("일시 차단은 5분 이상 30분 이하만 가능하다.")
    void whenBlockDurationOutOfRange_thenThrowException(int duration) {
        // given
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(eq(USER_ID), eq(CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(user);

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(creator.getUserId(), user.getUserId(), CHAT_ID, duration);
        }).isInstanceOf(InvalidBlockDurationException.class);
    }

    @Test
    @DisplayName("차단 상태의 사용자를 다시 차단할 수 없다.")
    void when_blockAlreadyBlockedUser_then_throwException() {
        // given
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(eq(USER_ID), eq(CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(user);
        willThrow(new AlreadyBlockedException("차단된 사용자입니다."))
            .given(chatValidationService).validateNotBlocked(user.getId());

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(creator.getUserId(), user.getUserId(), CHAT_ID, VALID_BLOCK_DURATION);
        });
    }

    @Test
    @DisplayName("차단된 사용자는 메시지를 전송할 수 없다.")
    void given_blockedUser_when_sendMessage_then_throwException() {
        // given
        Participation blockedUser = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(blockedUser.getUserId(), CHAT_ID)).willReturn(blockedUser);
        willThrow(new AlreadyBlockedException("차단된 사용자입니다."))
            .given(chatValidationService).validateNotBlocked(blockedUser.getId());

        // when, then
        assertThatThrownBy(() -> {
            chatService.sendMessage(blockedUser.getUserId(), CHAT_ID, "message");
        }).isInstanceOf(UnauthorizedSendMessageException.class);
    }

    @Test
    @DisplayName("현재 차단 상태인 참가자의 차단을 철회(해제)할 수 있다.")
    void given_blockedUser_when_unblock_then_retractBlock() {
        // given
        ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);
        Block block = Block.blockFor(BLOCK_ID, user.getId(), LocalDateTime.now(), VALID_BLOCK_DURATION);

        given(chatValidationService.requireParticipation(eq(USER_ID), eq(CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(user);
        given(blockRepository.findActiveBlockByParticipationId(eq(user.getId()), any(LocalDateTime.class)))
            .willReturn(Optional.of(block));

        // when
        chatService.unblockUser(creator.getUserId(), user.getUserId(), CHAT_ID);

        // then
        then(blockRepository).should().save(captor.capture());
        assertThat(captor.getValue().isRetracted()).isTrue();
    }

    @Test
    @DisplayName("차단되지 않은 참가자를 차단 해제할 수 없다.")
    void given_notBlockedUser_when_unblock_then_throwException() {
        // given
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID, CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(eq(USER_ID), eq(CHAT_ID))).willReturn(creator);
        given(chatValidationService.requireParticipation(eq(ANOTHER_USER_ID), eq(CHAT_ID))).willReturn(user);
        given(blockRepository.findActiveBlockByParticipationId(eq(user.getId()), any(LocalDateTime.class)))
            .willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> {
            chatService.unblockUser(creator.getUserId(), user.getUserId(), CHAT_ID);
        }).isInstanceOf(NotBlockedException.class);
    }

    @Test
    @DisplayName("일반 참가자는 다른 참가자의 차단을 해제할 수 없다")
    void given_notAuthorizedUser_when_unblock_then_throwException() {
        // given
        Participation user1 = Participation.joinAsUser(PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);
        Participation user2 = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, THE_OTHER_USER_ID, CHAT_ID);

        given(chatValidationService.requireParticipation(eq(user1.getUserId()), eq(CHAT_ID))).willReturn(user1);
        given(chatValidationService.requireParticipation(eq(user2.getUserId()), eq(CHAT_ID))).willReturn(user2);

        // when, then
        assertThatThrownBy(() -> {
            chatService.unblockUser(user1.getUserId(), user2.getUserId(), CHAT_ID);
        }).isInstanceOf(UnauthorizedUnblockException.class);
    }

    private Participation getManager(Participation creator, String participationId, String userId) {
        Participation manager = Participation.joinAsUser(participationId, userId, creator.getChatId());
        creator.promoteToManager(manager);
        return manager;
    }
}
