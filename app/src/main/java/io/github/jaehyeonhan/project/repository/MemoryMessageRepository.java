package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Message;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MemoryMessageRepository implements MessageRepository {

    private static final Map<String, Message> map = new HashMap<>();

    @Override
    public Message save(Message message) {
        return map.put(message.getId(), message);
    }

    @Override
    public List<Message> findByUserIdAndChatId(String userId, String chatId) {
        return map.values().stream()
            .filter(m -> m.getUserId().equals(userId) && Objects.equals(m.getChatId(), chatId))
            .toList();
    }
}
