package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Participation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryParticipationRepository implements ParticipationRepository {

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
    public void deleteAll() {
        map.clear();
    }
}
