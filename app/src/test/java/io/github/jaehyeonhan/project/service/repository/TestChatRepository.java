package io.github.jaehyeonhan.project.service.repository;

import io.github.jaehyeonhan.project.repository.ChatRepository;

public interface TestChatRepository extends ChatRepository {
    void deleteAll();
}
