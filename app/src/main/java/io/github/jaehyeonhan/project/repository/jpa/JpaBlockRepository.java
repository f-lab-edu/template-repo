package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Block;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaBlockRepository extends JpaRepository<Block, String> {

    Optional<Block> findByParticipationIdAndRetractedFalseAndExpiresAtAfter(String participationId, LocalDateTime now);
}
