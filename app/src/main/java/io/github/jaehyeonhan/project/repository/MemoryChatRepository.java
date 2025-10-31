package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Chat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryChatRepository implements ChatRepository {

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
