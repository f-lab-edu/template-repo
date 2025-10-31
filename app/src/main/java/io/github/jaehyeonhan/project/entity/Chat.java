package io.github.jaehyeonhan.project.entity;

import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Chat {

    private String id;
    private String creatorId;

    @Setter
    private String title;

    public Chat(String id, String creatorId, String title) {
        this.id = id;
        this.creatorId = creatorId;

        if(title.isBlank()) {
            throw new InvalidChatTitleException("Title cannot be empty");
        }
    }
}
