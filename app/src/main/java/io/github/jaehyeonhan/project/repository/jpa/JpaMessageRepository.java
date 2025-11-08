package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Message;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMessageRepository extends JpaRepository<Message, String> {

    List<Message> findByUserIdAndChatIdAndCreatedAtAfter(String userId, String chatId,
        LocalDateTime lastRead);
}
