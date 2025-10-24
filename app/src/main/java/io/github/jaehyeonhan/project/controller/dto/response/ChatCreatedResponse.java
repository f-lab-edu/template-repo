package io.github.jaehyeonhan.project.controller.dto.response;

public class ChatCreatedResponse {
    private final String chatId;

    public ChatCreatedResponse(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return this.chatId;
    }
}
