package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.*;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatValidationService chatValidationService;

    private final ChatRepository chatRepository;
    private final ParticipationRepository participationRepository;
    private final MessageRepository messageRepository;
    private final BlockRepository blockRepository;

    private final IdGenerator idGenerator;
    private final Clock clock;

    @Transactional
    public String createChat(String userId, String title) {
        String chatId = idGenerator.generate();
        Chat chat = new Chat(chatId, userId, title);
        chatRepository.save(chat);

        String participationId = idGenerator.generate();
        Participation participation = Participation.joinAsCreator(participationId, userId, chatId);
        participationRepository.save(participation);

        return chat.getId();
    }

    public void join(String userId, String chatId) {
        chatValidationService.requireChat(chatId);

        // 중복 참가 요청은 바로 반환
        if (participationRepository.existsByUserIdAndChatId(userId, chatId)) {
            return;
        }

        String participationId = idGenerator.generate();
        Participation participation = Participation.joinAsUser(participationId, userId, chatId);
        participationRepository.save(participation);
    }

    public void sendMessage(String userId, String chatId, String content) {
        Participation participation = chatValidationService.requireParticipation(userId, chatId);

        try {
            chatValidationService.validateNotBlocked(participation.getId());
        } catch (AlreadyBlockedException e) {
            throw new UnauthorizedSendMessageException("차단되어 메시지를 전송할 수 없습니다.");
        }

        String messageId = idGenerator.generate();
        Message message = new Message(messageId, chatId, userId, content, LocalDateTime.now(clock));
        messageRepository.save(message);
    }

    public List<MessageDto> getMessageList(String userId, String chatId, LocalDateTime lastRead) {
        chatValidationService.requireParticipation(userId, chatId);

        return messageRepository.findMessagesAfterLastRead(chatId, lastRead).stream()
                                .map(MessageDto::from)
                                .toList();
    }

    public void blockUser(String actorUserId, String targetUserId, String chatId,
        int durationInMin) {
        chatValidationService.requireChat(chatId);
        Participation actor = chatValidationService.requireParticipation(actorUserId, chatId);
        Participation target = chatValidationService.requireParticipation(targetUserId, chatId);

        if (!actor.canBlock(target)) {
            throw new UnauthorizedBlockException("차단 권한이 없습니다.");
        }

        chatValidationService.validateNotBlocked(target.getId());

        String blockId = idGenerator.generate();
        Block block = Block.blockFor(blockId, target.getId(), LocalDateTime.now(clock), durationInMin);
        blockRepository.save(block);
    }

    public void unblockUser(String actorUserId, String targetUserId, String chatId) {
        chatValidationService.requireChat(chatId);
        Participation actor = chatValidationService.requireParticipation(actorUserId, chatId);
        Participation target = chatValidationService.requireParticipation(targetUserId, chatId);

        if (!actor.canUnblock(target)) {
            throw new UnauthorizedUnblockException("차단 해제 권한이 없습니다.");
        }

        Block block = blockRepository.findActiveBlockByParticipationId(target.getId(),
                                         LocalDateTime.now(clock))
                                     .orElseThrow(() -> new NotBlockedException("차단 상태가 아닙니다."));
        block.retract();
        blockRepository.save(block);
    }
}
