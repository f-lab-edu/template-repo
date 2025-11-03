package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Chat;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Swagger 구동을 위해 추가한 dummy 타입
 */
@Repository
public class FakeChatRepository implements ChatRepository{

    @Override
    public Chat save(Chat chat) {
        return null;
    }

    @Override
    public Optional<Chat> findById(String chatId) {
        return Optional.empty();
    }
}
