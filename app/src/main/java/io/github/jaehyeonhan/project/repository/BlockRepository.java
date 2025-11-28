package io.github.jaehyeonhan.project.repository;

import io.github.jaehyeonhan.project.entity.Block;
import java.time.LocalDateTime;
import java.util.Optional;

public interface BlockRepository {
    Block save(Block block);

    Optional<Block> findActiveBlockByParticipationId(String participationId, LocalDateTime now);
}
