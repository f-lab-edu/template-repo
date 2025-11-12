package io.github.jaehyeonhan.project.entity;

import static io.github.jaehyeonhan.project.util.ValidationUtils.requireNonNull;

import io.github.jaehyeonhan.project.exception.InvalidChatTitleException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Chat {

    @Id
    private String id;
    private String creatorId;

    private String title;

    public Chat(String id, String creatorId, String title) {
        this.id = requireNonNull(id, "id");
        this.creatorId = requireNonNull(creatorId, "creator id");
        this.title = requireNonNull(title, "title");

        validateTitle(title);
    }

    private void validateTitle(String title) {
        if (title.isBlank()) {
            throw new InvalidChatTitleException("Title cannot be empty");
        }

        if (title.length() > 100) {
            throw new InvalidChatTitleException("Title cannot be longer than 100 characters");
        }
    }
}
