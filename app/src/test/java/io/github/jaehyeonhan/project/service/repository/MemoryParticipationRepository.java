package io.github.jaehyeonhan.project.service.repository;

import io.github.jaehyeonhan.project.entity.Participation;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MemoryParticipationRepository implements TestParticipationRepository {

    private final static Map<String, Participation> map = new HashMap<>();

    @Override
    public Participation save(Participation participation) {
        return map.put(participation.getId(), participation);
    }

    @Override
    public Optional<Participation> findByUserIdAndChatId(String userId, String chatId) {
        return map.values().stream()
                  .filter(p -> p.getUserId().equals(userId) && p.getChatId().equals(chatId))
                  .findFirst();
    }

    @Override
    public int countByUserIdAndChatId(String userId, String chatId) {
        return findByUserIdAndChatId(userId, chatId).isPresent() ? 1 : 0;
    }

    @Override
    public void deleteAll() {
        map.clear();
    }
}
