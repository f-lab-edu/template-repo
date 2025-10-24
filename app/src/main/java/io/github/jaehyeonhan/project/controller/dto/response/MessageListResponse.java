package io.github.jaehyeonhan.project.controller.dto.response;

import java.util.List;

public class MessageListResponse {
    private final List<MessageResponse> messages;

    public MessageListResponse(List<MessageResponse> messages) {
        this.messages = messages;
    }

    public List<MessageResponse> getMessages() {
        return messages;
    }
}
