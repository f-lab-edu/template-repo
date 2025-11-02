package io.github.jaehyeonhan.project.service.repository;

import io.github.jaehyeonhan.project.repository.MessageRepository;

public interface TestMessageRepository extends MessageRepository {
    void deleteAll();
}
