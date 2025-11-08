package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Participation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaParticipationRepository extends JpaRepository<Participation, String> {

    Optional<Participation> findByUserIdAndChatId(String userId, String chatId);
}
