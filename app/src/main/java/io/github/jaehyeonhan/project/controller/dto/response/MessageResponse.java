package io.github.jaehyeonhan.project.controller.dto.response;

public class MessageResponse {
    private final String id;
    private final String content;

    public MessageResponse(String id, String content) {
        this.id = id;
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
