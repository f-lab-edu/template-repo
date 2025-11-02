package io.github.jaehyeonhan.project.controller;

import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.GetNewMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.request.JoinChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.SendMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.response.ChatCreatedResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageListResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageResponse;
import io.github.jaehyeonhan.project.service.ChatService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/chats"})
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatCreatedResponse> createChat(@RequestBody CreateChatRequest request) {
        String chatId = chatService.createChat(request.getUserId(), request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatCreatedResponse(chatId));
    }

    @PostMapping("/{chatId}/participants")
    public ResponseEntity<Void> joinChat(@RequestBody JoinChatRequest request,
        @PathVariable String chatId) {
        chatService.join(request.getUserId(), chatId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest request,
        @PathVariable String chatId) {
        chatService.sendMessage(request.getUserId(), chatId, request.getContent());
        return ResponseEntity.ok().build();
    }

    @GetMapping("{chatId}/messages")
    public ResponseEntity<MessageListResponse> getNewMessageList(
        @ModelAttribute GetNewMessageRequest request, @PathVariable String chatId) {
        List<MessageResponse> messageResponses = chatService.getMessageList(request.getUserId(),
                                                                chatId,
                                                                request.getLastRead())
                                                            .stream()
                                                            .map(MessageResponse::from)
                                                            .toList();

        return ResponseEntity.ok(new MessageListResponse(messageResponses));
    }
}
