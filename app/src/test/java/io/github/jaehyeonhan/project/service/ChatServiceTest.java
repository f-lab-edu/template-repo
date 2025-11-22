package io.github.jaehyeonhan.project.service;

import static io.github.jaehyeonhan.project.service.ChatConst.ANOTHER_PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.ANOTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.BEGINNING_OF_TIME;
import static io.github.jaehyeonhan.project.service.ChatConst.BLOCK_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.MESSAGE_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.NON_EXISTENT_CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.THE_OTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.USER_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.VALID_BLOCK_DURATION;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
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
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

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
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);

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
        Chat chat = new Chat(CHAT_ID, ANOTHER_USER_ID, "title");
        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.empty());
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
        Chat chat = new Chat(CHAT_ID, ANOTHER_USER_ID, "title");
        Participation participation = Participation.joinAsUser(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(chatRepository.findById(ChatConst.CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.empty(), Optional.of(participation));
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
        given(chatRepository.findById(NON_EXISTENT_CHAT_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> chatService.join(USER_ID, NON_EXISTENT_CHAT_ID))
            .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("참여한 채팅에 메시지 전송 시 메시지가 저장된다.")
    void given_userJoinedChat_when_sendMessage_then_messageIsSaved() {
        // given
        Message message = new Message(MESSAGE_ID, CHAT_ID, USER_ID, "content");
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.of(participation));
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
        given(participationRepository.findByUserIdAndChatId(USER_ID,
            CHAT_ID)).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> chatService.sendMessage(USER_ID, CHAT_ID, "content"))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("참여한 채팅의 새 메시지 조회 시 메시지를 응답한다.")
    void given_userJoinedChat_when_getNewMessage_then_messageListIsReturned() {
        // given
        Participation participation = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Message message = new Message(MESSAGE_ID, CHAT_ID, USER_ID, "content");

        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.of(participation));
        given(messageRepository.findMessagesAfterLastRead(CHAT_ID,
            BEGINNING_OF_TIME)).willReturn(List.of(message));

        // when
        List<MessageDto> newMessageList = chatService.getMessageList(USER_ID, CHAT_ID,
            BEGINNING_OF_TIME);

        // then
        assertThat(newMessageList).anyMatch(d -> d.getId().equals(MESSAGE_ID));
    }

    @Test
    @DisplayName("참여하지 않은 채팅의 새 메시지 조회 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_getNewMessage_then_throwException() {
        // given
        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.empty());

        // when, then
        assertThatThrownBy(() -> chatService.getMessageList(USER_ID, CHAT_ID, BEGINNING_OF_TIME))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("방장과 관리자는 다른 일반 참가자의 메시지 전송을 차단할 수 있다.")
    void given_chatCreatorAndManager_when_blockUser_then_userIsBlocked() {
        // given
        ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(PARTICIPATION_ID, ANOTHER_USER_ID, CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));

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
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(user.getUserId(), creator.getUserId(), CHAT_ID,
                VALID_BLOCK_DURATION);
        }).isInstanceOf(UnauthorizedBlockException.class);
    }

    @Test
    @DisplayName("관리자는 서로의 메시지 전송을 차단할 수 없다.")
    void given_chatManager_when_blockEachOther_then_throwException() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        String manager1ParticipationId = "619da13e-8564-43b1-9913-fbabfe6c61c1";
        Participation manager1 = getManager(creator, manager1ParticipationId, ANOTHER_USER_ID);
        String manager2ParticipationId = "449388e5-1544-48cc-91e0-8c2363713236";
        Participation manager2 = getManager(creator, manager2ParticipationId, THE_OTHER_USER_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(
            Optional.of(manager1));
        given(participationRepository.findByUserIdAndChatId(eq(THE_OTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(manager2));

        // when, then
        assertThatThrownBy(
            () -> chatService.blockUser(manager1.getUserId(), manager2.getUserId(), chat.getId(),
                VALID_BLOCK_DURATION))
            .isInstanceOf(UnauthorizedBlockException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {4, 31, -1})
    @DisplayName("일시 차단은 5분 이상 30분 이하만 가능하다.")
    void whenBlockDurationOutOfRange_thenThrowException(int duration) {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));
        given(idGenerator.generate()).willReturn(BLOCK_ID);

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(creator.getUserId(), user.getUserId(), chat.getId(), duration);
        }).isInstanceOf(InvalidBlockDurationException.class);
    }

    @Test
    @DisplayName("차단 상태의 사용자를 다시 차단할 수 없다.")
    void when_blockAlreadyBlockedUser_then_throwException() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);
        Block block = Block.blockFor(BLOCK_ID, user.getId(), VALID_BLOCK_DURATION);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));
        given(blockRepository.findActiveBlockByParticipationId(user.getId(),
            LocalDateTime.now())).willReturn(
            Optional.of(block));

        // when, then
        assertThatThrownBy(() -> {
            chatService.blockUser(creator.getUserId(), user.getUserId(), chat.getId(),
                VALID_BLOCK_DURATION);
        });
    }

    @Test
    @DisplayName("차단된 사용자는 메시지를 전송할 수 없다.")
    void given_blockedUser_when_sendMessage_then_throwException() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation blockedUser = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID,
            ANOTHER_USER_ID, chat.getId());
        Block block = Block.blockFor(BLOCK_ID, blockedUser.getId(), 0);

        given(participationRepository.findByUserIdAndChatId(blockedUser.getUserId(),
            chat.getId())).willReturn(
            Optional.of(blockedUser));
        given(blockRepository.findActiveBlockByParticipationId(blockedUser.getId(),
            LocalDateTime.now())).willReturn(
            Optional.of(block));

        // when, then
        assertThatThrownBy(() -> {
            chatService.sendMessage(blockedUser.getUserId(), chat.getId(), "message");
        }).isInstanceOf(UnauthorizedSendMessageException.class);
    }

    @Test
    @DisplayName("현재 차단 상태인 참가자의 차단을 철회(해제)할 수 있다.")
    void given_blockedUser_when_unblock_then_retractBlock() {
        // given
        ArgumentCaptor<Block> captor = ArgumentCaptor.forClass(Block.class);
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);
        Block block = Block.blockFor(BLOCK_ID, user.getId(), VALID_BLOCK_DURATION);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));
        given(blockRepository.findActiveBlockByParticipationId(eq(user.getId()),
            any(LocalDateTime.class))).willReturn(
            Optional.of(
                block)); // fixme: potential regression not verifying calling LocalDateTime.now()

        // when
        chatService.unblockUser(creator.getUserId(), user.getUserId(), chat.getId());

        // then
        then(blockRepository).should().save(captor.capture());
        assertThat(captor.getValue().isRetracted()).isTrue();
    }

    @Test
    @DisplayName("차단되지 않은 참가자를 차단 해제할 수 없다.")
    void given_notBlockedUser_when_unblock_then_throwException() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation creator = Participation.joinAsCreator(PARTICIPATION_ID, USER_ID,
            CHAT_ID);
        Participation user = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(USER_ID), eq(CHAT_ID))).willReturn(
            Optional.of(creator));
        given(participationRepository.findByUserIdAndChatId(eq(ANOTHER_USER_ID),
            eq(CHAT_ID))).willReturn(Optional.of(user));
        given(blockRepository.findActiveBlockByParticipationId(eq(user.getId()),
            any(LocalDateTime.class))).willReturn(Optional.empty());

        // when, then
        assertThatThrownBy(() -> {
            chatService.unblockUser(creator.getUserId(), user.getUserId(), chat.getId());
        }).isInstanceOf(NotBlockedException.class);
    }

    @Test
    @DisplayName("일반 참가자는 다른 참가자의 차단을 해제할 수 없다")
    void given_notAuthorizedUser_when_unblock_then_throwException() {
        // given
        Chat chat = new Chat(CHAT_ID, USER_ID, "title");
        Participation user1 = Participation.joinAsUser(PARTICIPATION_ID, ANOTHER_USER_ID,
            CHAT_ID);
        Participation user2 = Participation.joinAsUser(ANOTHER_PARTICIPATION_ID, THE_OTHER_USER_ID,
            CHAT_ID);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(eq(user1.getUserId()),
            eq(chat.getId()))).willReturn(
            Optional.of(user1));
        given(participationRepository.findByUserIdAndChatId(eq(user2.getUserId()),
            eq(chat.getId()))).willReturn(Optional.of(user2));

        // when, then
        assertThatThrownBy(() -> {
            chatService.unblockUser(user1.getUserId(), user2.getUserId(), chat.getId());
        }).isInstanceOf(UnauthorizedUnblockException.class);
    }

    private Participation getManager(Participation creator, String participationId, String userId) {
        Participation manager = Participation.joinAsUser(participationId, userId,
            creator.getChatId());
        creator.promoteToManager(manager);
        return manager;
    }
}