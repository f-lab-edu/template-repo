package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Chat;
import java.util.Optional;

public interface ChatRepository {

    Chat save(Chat chat);

    Optional<Chat> findById(String chatId);
}
