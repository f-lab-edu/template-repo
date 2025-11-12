package io.github.jaehyeonhan.project.service;

import static io.github.jaehyeonhan.project.service.ChatConst.ANOTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.BEGINNING_OF_TIME;
import static io.github.jaehyeonhan.project.service.ChatConst.CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.MESSAGE_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.NON_EXISTENT_CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, CHAT_ID);

        given(idGenerator.generate()).willReturn(CHAT_ID, PARTICIPATION_ID);
        given(chatRepository.save(any(Chat.class))).willReturn(chat);

        given(chatRepository.findById(CHAT_ID)).willReturn(Optional.of(chat));
        given(participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).willReturn(
            Optional.empty());
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
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, CHAT_ID);

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
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, CHAT_ID);

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
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, CHAT_ID);

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
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, CHAT_ID);
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

    private String getNewParticipationId() {
        return UUID.randomUUID().toString();
    }

    private String getNewChatId() {
        return UUID.randomUUID().toString();
    }

    private String getNewMessageId() {
        return UUID.randomUUID().toString();
    }
}