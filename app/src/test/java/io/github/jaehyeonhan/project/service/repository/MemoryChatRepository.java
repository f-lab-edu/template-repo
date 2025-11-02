package io.github.jaehyeonhan.project.service.repository;

import io.github.jaehyeonhan.project.entity.Chat;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryChatRepository implements TestChatRepository {

    private static final Map<String, Chat> map = new HashMap<>();

    @Override
    public Chat save(Chat chat) {
        return map.put(chat.getId(), chat);
    }

    @Override
    public Optional<Chat> findById(String chatId) {
        return Optional.ofNullable(map.get(chatId));
    }

    @Override
    public void deleteAll() {
        map.clear();
    }
}
