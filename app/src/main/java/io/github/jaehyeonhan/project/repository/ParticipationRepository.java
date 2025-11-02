package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Participation;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParticipationRepository {

    Participation save(Participation participation);

    Optional<Participation> findByUserIdAndChatId(String userId, String chatId);
}
