package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 * Swagger 구동을 위해 추가한 dummy 타입
 */
@Repository
public class FakeMessageRepository implements MessageRepository {

    @Override
    public Message save(Message message) {
        return null;
    }

    @Override
    public List<Message> findMessagesAfterLastRead(String userId, String chatId,
        LocalDateTime lastRead) {
        return List.of();
    }
}
