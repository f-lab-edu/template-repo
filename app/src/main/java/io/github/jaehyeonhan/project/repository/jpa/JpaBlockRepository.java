package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Block;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JpaBlockRepository extends JpaRepository<Block, String> {

    Optional<Block> findByParticipationIdAndRetractedFalseAndExpiresAtAfter(String participationId, LocalDateTime now);
}
