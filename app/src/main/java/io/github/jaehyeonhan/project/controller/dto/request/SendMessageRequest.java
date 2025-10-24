package io.github.jaehyeonhan.project.controller.dto.request;

public class SendMessageRequest {

    private final String content;

    public SendMessageRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
