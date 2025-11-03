package io.github.jaehyeonhan.project.controller;

import io.github.jaehyeonhan.project.controller.dto.request.CreateChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.GetNewMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.request.JoinChatRequest;
import io.github.jaehyeonhan.project.controller.dto.request.SendMessageRequest;
import io.github.jaehyeonhan.project.controller.dto.response.ChatCreatedResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageListResponse;
import io.github.jaehyeonhan.project.controller.dto.response.MessageResponse;
import io.github.jaehyeonhan.project.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "채팅", description = "채팅 및 메시지 송수신 관련 API")
@RestController
@RequestMapping({"/api/chats"})
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @Operation(
        summary = "채팅 생성",
        description = "새로운 채팅을 생성하고 참가합니다.",
        responses = {
            @ApiResponse(responseCode = "201", description = "정상 생성")
        }
    )
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChatCreatedResponse> createChat(@RequestBody CreateChatRequest request) {
        String chatId = chatService.createChat(request.getUserId(), request.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED).body(new ChatCreatedResponse(chatId));
    }

    @Operation(
        summary = "채팅 참가",
        description = "생성된 채팅에 참가합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "정상 참가")
        }
    )
    @PostMapping("/{chatId}/participants")
    public ResponseEntity<Void> joinChat(@RequestBody JoinChatRequest request,
        @PathVariable String chatId) {
        chatService.join(request.getUserId(), chatId);
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "채팅 메시지 전송",
        description = "채팅에 메시지를 전송합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "정상 전송")
        }
    )
    @PostMapping("/{chatId}/messages")
    public ResponseEntity<Void> sendMessage(@RequestBody SendMessageRequest request,
        @PathVariable String chatId) {
        chatService.sendMessage(request.getUserId(), chatId, request.getContent());
        return ResponseEntity.ok().build();
    }

    @Operation(
        summary = "새 채팅 메시지 조회",
        description = "lastRead 이후의 채팅 메시지를 조회합니다.",
        responses = {
            @ApiResponse(responseCode = "200", description = "정상 조회")
        }
    )
    @GetMapping(value = "{chatId}/messages", produces = MediaType.APPLICATION_JSON_VALUE)
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
