package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Participation;
import java.util.Optional;

public interface ParticipationRepository {

    Participation save(Participation participation);

    Optional<Participation> findByUserIdAndChatId(String userId, String chatId);

    void deleteAll();
}
