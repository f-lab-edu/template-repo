package io.github.jaehyeonhan.project.service.repository;

import io.github.jaehyeonhan.project.repository.ParticipationRepository;

public interface TestParticipationRepository extends ParticipationRepository {
    int countByUserIdAndChatId(String chatId, String userId);

    void deleteAll();
}
