package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepository {

    private final JpaParticipationRepository jpaParticipationRepository;

    @Override
    public Participation save(Participation participation) {
        return jpaParticipationRepository.save(participation);
    }

    @Override
    public Optional<Participation> findByUserIdAndChatId(String userId, String chatId) {
        return jpaParticipationRepository.findByUserIdAndChatId(userId, chatId);
    }
}
