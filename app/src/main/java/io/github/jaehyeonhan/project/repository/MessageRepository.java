package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Message;
import java.util.List;

public interface MessageRepository {

    Message save(Message message);

    List<Message> findByUserIdAndChatId(String userId, String chatId);
}
