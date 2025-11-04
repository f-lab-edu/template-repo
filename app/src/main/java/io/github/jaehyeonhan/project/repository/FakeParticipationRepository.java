package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Participation;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Swagger 구동을 위해 추가한 dummy 타입
 */
@Repository
public class FakeParticipationRepository implements ParticipationRepository {

    @Override
    public Participation save(Participation participation) {
        return null;
    }

    @Override
    public Optional<Participation> findByUserIdAndChatId(String userId, String chatId) {
        return Optional.empty();
    }
}
