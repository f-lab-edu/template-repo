package io.github.jaehyeonhan.project.controller.dto.request;

public class CreateChatRequest {
    private final String title;

    public CreateChatRequest(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }
}
