package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChatRepository extends JpaRepository<Chat, String> {

}
