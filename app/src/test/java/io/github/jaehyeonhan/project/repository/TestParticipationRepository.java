package io.github.jaehyeonhan.project.repository;

public interface TestParticipationRepository extends ParticipationRepository {
    int countByUserIdAndChatId(String chatId, String userId);

    void deleteAll();
}
