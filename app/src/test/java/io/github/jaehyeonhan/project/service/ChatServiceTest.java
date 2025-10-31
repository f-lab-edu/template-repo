package io.github.jaehyeonhan.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MemoryChatRepository;
import io.github.jaehyeonhan.project.repository.MemoryMessageRepository;
import io.github.jaehyeonhan.project.repository.MemoryParticipationRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ChatServiceTest {

    private static final String FIRST_READ = "0";
    private static final String USER_ID = "1";
    private static final String EXISTENT_CHAT_ID = "22";
    private static final String NON_EXISTENT_CHAT_ID = "9999999";
    private static final String PARTICIPATION_ID = "333";

    private final ChatRepository chatRepository = new MemoryChatRepository();
    private final ParticipationRepository participationRepository = new MemoryParticipationRepository();
    private final MessageRepository messageRepository = new MemoryMessageRepository();

    private final ChatService chatService = new ChatService(chatRepository, participationRepository, messageRepository, new IdGenerator());

    /*
    테스트 메소드 명명 스타일
    given_조건_when_동작_then_결과
    */
    @Test
    @DisplayName("채팅 정상 생성 시 chat이 생성되고, 요청자가 참가자로 추가된다.")
    void given_title_when_create_then_createChatAndRequesterJoinsChat() {
        // given
        String title = "test";

        // when
        String chatId = chatService.create(USER_ID, title);

        // then
        assertThat(chatId).isNotNull();
        assertThat(chatRepository.findById(chatId)).isNotNull();
        assertThat(participationRepository.findByUserIdAndChatId(USER_ID, chatId)).isNotNull();
    }

    @Test
    @DisplayName("빈 제목으로 채팅 생성 시 예외가 발생한다.")
    void given_emptyTitle_when_create_then_throwException() {
        // given
        String emptyTitle = "";

        // when, then
        assertThatThrownBy(() -> chatService.create(USER_ID, emptyTitle))
            .isInstanceOf(InvalidChatTitleException.class);
    }

    @Test
    @DisplayName("채팅이 존재할 시 채팅에 참가한다.")
    void given_chatExists_when_join_then_userJoinsChat() {
        // given
        createChat();

        // when
        chatService.join(USER_ID, EXISTENT_CHAT_ID);

        // then
        assertThat(participationRepository.findByUserIdAndChatId(USER_ID, EXISTENT_CHAT_ID)).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 채팅에 참여할 시 예외가 발생한다.")
    void given_chatNotExists_when_join_then_throwException() {
        // given

        // when, then
        assertThatThrownBy(() -> chatService.join(USER_ID, NON_EXISTENT_CHAT_ID))
            .isInstanceOf(ChatNotFoundException.class);
    }

    @Test
    @DisplayName("참여한 채팅에 메시지 전송 시 메시지가 저장된다.")
    void given_userJoinedChat_when_sendMessage_then_messageIsSaved() {
        // given
        createChat();
        createParticipation();

        // when
        chatService.sendMessage(USER_ID, EXISTENT_CHAT_ID, "content");

        // then
        List<Message> messageList = messageRepository.findByUserIdAndChatId(USER_ID,
            EXISTENT_CHAT_ID);
        assertThat(messageList).isNotEmpty();
    }

    @Test
    @DisplayName("참여하지 않은 채팅에 메시지 전송 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_sendMessage_then_throwException() {
        // given
        createChat();

        // when, then
        assertThatThrownBy(() -> chatService.sendMessage(USER_ID, EXISTENT_CHAT_ID, "content"))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("참여한 채팅의 새 메시지 조회 시 메시지를 응답한다.")
    void given_userJoinedChat_when_getNewMessage_then_messageListIsReturned() {
        // given
        createChat();
        createParticipation();

        String messageId = "4444";
        Message message = new Message(messageId, EXISTENT_CHAT_ID, USER_ID, "content");
        messageRepository.save(message);

        // when
        List<MessageDto> newMessageList = chatService.getNewMessageList(USER_ID, EXISTENT_CHAT_ID,
            FIRST_READ);

        // then
        assertThat(newMessageList).anyMatch(d -> d.getId().equals(messageId));
    }

    @Test
    @DisplayName("참여하지 않은 채팅의 새 메시지 조회 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_getNewMessage_then_throwException() {
        // given
        createChat();

        // when, then
        assertThatThrownBy(() -> chatService.getNewMessageList(USER_ID, NON_EXISTENT_CHAT_ID, FIRST_READ))
            .isInstanceOf(NotParticipatingException.class);
    }

    private void createChat() {
        Chat chat = new Chat(EXISTENT_CHAT_ID, USER_ID, "title");
        chatRepository.save(chat);
    }

    private void createParticipation() {
        Participation participation = new Participation(PARTICIPATION_ID, USER_ID, EXISTENT_CHAT_ID);
        participationRepository.save(participation);
    }
}