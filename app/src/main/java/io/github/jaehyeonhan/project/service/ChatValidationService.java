package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.entity.ParticipationCache;
import io.github.jaehyeonhan.project.exception.AlreadyBlockedException;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatValidationService {

    private final ChatRepository chatRepository;
    private final ParticipationCacheService participationCacheService;
    private final BlockRepository blockRepository;

    private final Clock clock;

    public void requireChat(String chatId) {
        chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("채팅이 없습니다."));
    }

    public Participation requireParticipation(String userId, String chatId) {
        ParticipationCache cache = participationCacheService.get(userId, chatId);
        if (cache == null) {
            throw new NotParticipatingException("참여 중인 채팅이 아닙니다.");
        }
        return cache.toParticipation(userId, chatId);
    }

    public void validateNotBlocked(String participationId) {
        Optional<Block> optionalBlock = blockRepository.findActiveBlockByParticipationId(
            participationId, LocalDateTime.now(clock));
        if (optionalBlock.isPresent()) {
            throw new AlreadyBlockedException("차단된 사용자입니다.");
        }
    }
}
