package io.github.jaehyeonhan.project;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.JoinChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.SendMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.response.ChatCreatedResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageListResponse;
import io.github.jaehyeonhan.project.service.ChatService;
import java.net.URI;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class ApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ChatService chatService;

    @Test
    @DisplayName("채팅 생성 시 201 상태코드와 chatId 문자열을 반환한다.")
    void given_createChatRequest_when_createChat_then_return201WithChatId() {
        // given
        CreateChatRequest request = new CreateChatRequest("user1", "title");

        // when
        ResponseEntity<ChatCreatedResponse> response = restTemplate.postForEntity(
            "/api/chats", request, ChatCreatedResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Objects.requireNonNull(response.getBody()).getChatId()).isNotEmpty();
    }

    @Test
    @DisplayName("채팅 정상 참가 시 200 상태코드를 반환한다.")
    void given_chatExists_when_join_returns200() {
        // given
        String chatId = chatService.createChat("user1", "title");
        JoinChatRequest request = new JoinChatRequest("user2");

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/chats/" + chatId + "/participants", request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("참가한 채팅에 메시지를 보낼 시 200 상태코드를 반환한다.")
    void given_chatExistsAndParticipating_when_sendMessage_return200() {
        // given
        String chatId = chatService.createChat("user1", "title");
        SendMessageRequest request = new SendMessageRequest("user1", "message");

        // when
        ResponseEntity<Void> response = restTemplate.postForEntity(
            "/api/chats/" + chatId + "/messages", request, Void.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("참가한 채팅에 새 메시지가 있는 경우 200 상태코드와 메시지 목록을 반환한다.")
    void given_participatingInChat_when_getNewMessageList_then_return200WithMessages() {
        // given
        String chatId = chatService.createChat("user1", "title");
        chatService.join("user2", chatId);
        chatService.sendMessage("user1", chatId, "content");

        String lastRead = "1970-01-01T00:00:00";

        URI uri = UriComponentsBuilder
            .fromUriString("/api/chats/" + chatId + "/messages")
            .queryParam("userId", "user2")
            .queryParam("lastRead", lastRead)
            .build()
            .toUri();

        // when
        ResponseEntity<MessageListResponse> response = restTemplate.getForEntity(uri,
            MessageListResponse.class);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getMessages().size()).isPositive();
    }
}