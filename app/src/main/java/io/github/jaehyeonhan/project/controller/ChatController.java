package io.github.jaehyeonhan.project.controller;

import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.response.ChatCreatedResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/chats"})
public class ChatController {

    @PostMapping
    public ResponseEntity<ChatCreatedResponse> createChat(@RequestBody CreateChatRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatCreatedResponse("1234"));
    }
}
