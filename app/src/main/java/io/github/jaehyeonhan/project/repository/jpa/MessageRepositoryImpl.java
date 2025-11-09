package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepositoryImpl implements MessageRepository {

    private final JpaMessageRepository jpaMessageRepository;

    @Override
    public Message save(Message chat) {
        return jpaMessageRepository.save(chat);
    }

    @Override
    public List<Message> findMessagesAfterLastRead(String chatId, LocalDateTime lastRead) {
        return jpaMessageRepository.findByChatIdAndCreatedAtAfter(chatId, lastRead);
    }
}
