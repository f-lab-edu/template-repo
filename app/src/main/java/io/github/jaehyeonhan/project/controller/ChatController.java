package io.github.jaehyeonhan.project.controller;

import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.SendMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.response.ChatCreatedResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageListResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageResponse;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/chats"})
public class ChatController {

    @PostMapping
    public ResponseEntity<ChatCreatedResponse> createChat(@RequestBody CreateChatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatCreatedResponse("1234"));
    }

    @PostMapping("/{chatId}")
    public ResponseEntity<Void> joinChat(@PathVariable String chatId) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest request) {
        return ResponseEntity.ok().build();
    }

    @GetMapping("{chatId}/messages")
    public ResponseEntity<MessageListResponse> getNewMessageList(@RequestParam String lastRead) {
        return ResponseEntity.ok(new MessageListResponse(List.of(new MessageResponse("1", "this is a message"))));
    }
}
