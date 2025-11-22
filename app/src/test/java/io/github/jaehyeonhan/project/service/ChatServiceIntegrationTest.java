package io.github.jaehyeonhan.project.service;

import static io.github.jaehyeonhan.project.service.ChatConst.ANOTHER_USER_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.BEGINNING_OF_TIME;
import static io.github.jaehyeonhan.project.service.ChatConst.BLOCK_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.NON_EXISTENT_CHAT_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.PARTICIPATION_ID;
import static io.github.jaehyeonhan.project.service.ChatConst.USER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.ParticipationRole;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.repository.jpa.BlockRepositoryImpl;
import io.github.jaehyeonhan.project.repository.jpa.ChatRepositoryImpl;
import io.github.jaehyeonhan.project.repository.jpa.MessageRepositoryImpl;
import io.github.jaehyeonhan.project.repository.jpa.ParticipationRepositoryImpl;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Import({ChatService.class, ChatRepositoryImpl.class, ParticipationRepositoryImpl.class,
    MessageRepositoryImpl.class, BlockRepositoryImpl.class, IdGenerator.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatServiceIntegrationTest {

    @BeforeEach
    @Sql("/clear-tables.sql")
    void clearTables() {
    }

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRepository chatRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Test
    @DisplayName("채팅 정상 생성 시 chat이 생성되고, 요청자가 참가자로 추가된다.")
    void given_title_when_create_then_createChatAndRequesterJoinsChat() {
        // given
        String title = "test";

        // when
        String chatId = chatService.createChat(USER_ID, title);

        // then
        assertThat(chatId).isNotNull();
        assertThat(chatRepository.findById(chatId)).isNotNull();
        assertThat(participationRepository.findByUserIdAndChatId(USER_ID, chatId)).isNotNull();
    }

    @Test
    @DisplayName("빈 제목으로 채팅 생성 시 예외가 발생한다.")
    void given_emptyTitle_when_createChat_then_throwException() {
        // given
        String emptyTitle = "";

        // when, then
        assertThatThrownBy(() -> chatService.createChat(USER_ID, emptyTitle))
            .isInstanceOf(InvalidChatTitleException.class);
    }

    @Test
    @DisplayName("채팅이 존재할 시 채팅에 참가한다.")
    void given_chatExists_when_join_then_userJoinsChat() {
        // given
        createChat(ANOTHER_USER_ID, CHAT_ID);

        // when
        chatService.join(USER_ID, CHAT_ID);

        // then
        assertThat(
            participationRepository.findByUserIdAndChatId(USER_ID, CHAT_ID)).isNotNull();
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
        createChat(USER_ID, CHAT_ID);
        joinChatAs(PARTICIPATION_ID, USER_ID, CHAT_ID, ParticipationRole.CREATOR);

        // when
        chatService.sendMessage(USER_ID, CHAT_ID, "content");

        // then
        List<Message> messageList = messageRepository.findMessagesAfterLastRead(CHAT_ID, BEGINNING_OF_TIME);
        assertThat(messageList).isNotEmpty();
    }

    @Test
    @DisplayName("참여하지 않은 채팅에 메시지 전송 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_sendMessage_then_throwException() {
        // given
        createChat(ANOTHER_USER_ID, CHAT_ID);

        // when, then
        assertThatThrownBy(() -> chatService.sendMessage(USER_ID, CHAT_ID, "content"))
            .isInstanceOf(NotParticipatingException.class);
    }

    @Test
    @DisplayName("참여한 채팅의 새 메시지 조회 시 메시지를 응답한다.")
    void given_userJoinedChat_when_getNewMessage_then_messageListIsReturned() {
        // given
        createChat(ANOTHER_USER_ID, CHAT_ID);
        joinChatAs(PARTICIPATION_ID, USER_ID, CHAT_ID, ParticipationRole.USER);

        String messageId = "4444";
        Message message = new Message(messageId, CHAT_ID, USER_ID, "content");
        messageRepository.save(message);

        // when
        List<MessageDto> newMessageList = chatService.getMessageList(USER_ID, CHAT_ID,
            BEGINNING_OF_TIME);

        // then
        assertThat(newMessageList).anyMatch(d -> d.getId().equals(messageId));
    }

    @Test
    @DisplayName("참여하지 않은 채팅의 새 메시지 조회 시 예외가 발생한다.")
    void given_userNotJoinedChat_when_getNewMessage_then_throwException() {
        // given
        createChat(CHAT_ID, ANOTHER_USER_ID);

        // when, then
        assertThatThrownBy(() -> chatService.getMessageList(USER_ID, CHAT_ID,
            BEGINNING_OF_TIME))
            .isInstanceOf(NotParticipatingException.class);
    }

    private void createChat(String userId, String chatId) {
        Chat chat = new Chat(chatId, userId, "title");
        chatRepository.save(chat);
    }

    private void joinChatAs(String participationId, String userId, String chatId, ParticipationRole role) {
        Participation participation;
        if(role.equals(ParticipationRole.CREATOR)) {
            participation = Participation.joinAsCreator(participationId, userId, chatId);
        } else if(role.equals(ParticipationRole.USER)) {
            participation = Participation.joinAsUser(participationId, userId, chatId);
        } else {
            throw new IllegalArgumentException("role 재확인");
        }
        participationRepository.save(participation);
    }

    @Test
    @DisplayName("차단이 해제된 사용자는 정상적으로 메시지를 전송할 수 있다.")
    void given_unblockedUser_when_sendMessage_then_messageIsSent() {
        // given
        createChat(ANOTHER_USER_ID, CHAT_ID);
        joinChatAs(PARTICIPATION_ID, USER_ID, CHAT_ID, ParticipationRole.USER);
        Block block = Block.blockFor(BLOCK_ID, PARTICIPATION_ID, 0);
        block.retract();
        blockRepository.save(block);

        // when
        chatService.sendMessage(USER_ID, CHAT_ID, "content");

        // then
        List<Message> messageList = messageRepository.findMessagesAfterLastRead(CHAT_ID, BEGINNING_OF_TIME);
        assertThat(messageList).isNotEmpty();
    }
}

