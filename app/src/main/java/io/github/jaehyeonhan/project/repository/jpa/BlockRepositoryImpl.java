package io.github.jaehyeonhan.project.repository.jpa;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BlockRepositoryImpl implements BlockRepository {

    private final JpaBlockRepository blockRepository;

    @Override
    public Block save(Block block) {
        return blockRepository.save(block);
    }

    @Override
    public Optional<Block> findActiveBlockByParticipationId(String participationId, LocalDateTime now) {
        return blockRepository.findByParticipationIdAndRetractedFalseAndExpiresAtAfter(participationId, now);
    }
}
