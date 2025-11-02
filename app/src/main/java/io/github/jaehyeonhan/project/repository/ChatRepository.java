package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Chat;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRepository {

    Chat save(Chat chat);

    Optional<Chat> findById(String chatId);
}
