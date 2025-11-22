package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Block;
import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.AlreadyBlockedException;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.exception.UnauthorizedBlockException;
import io.github.jaehyeonhan.project.exception.UnauthorizedSendMessageException;
import io.github.jaehyeonhan.project.repository.BlockRepository;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipationRepository participationRepository;
    private final MessageRepository messageRepository;
    private final BlockRepository blockRepository;

    private final IdGenerator idGenerator;

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
        requireChat(chatId);

        // 중복 참가 요청은 바로 반환
        Optional<Participation> optParticipation = participationRepository.findByUserIdAndChatId(
            userId, chatId);
        if (optParticipation.isPresent()) {
            return;
        }

        String participationId = idGenerator.generate();
        Participation participation = Participation.joinAsUser(participationId, userId, chatId);
        participationRepository.save(participation);
    }

    private void requireChat(String chatId) {
        chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("채팅이 없습니다."));
    }

    public void sendMessage(String userId, String chatId, String content) {
        Participation participation = requireParticipation(userId, chatId);

        try {
            validateNotBlocked(participation.getId());
        } catch (AlreadyBlockedException e) {
            throw new UnauthorizedSendMessageException("차단되어 메시지를 전송할 수 없습니다.");
        }

        String messageId = idGenerator.generate();
        Message message = new Message(messageId, chatId, userId, content);
        messageRepository.save(message);
    }

    public List<MessageDto> getMessageList(String userId, String chatId, LocalDateTime lastRead) {
        requireParticipation(userId, chatId);

        return messageRepository.findMessagesAfterLastRead(chatId, lastRead).stream()
                                .map(MessageDto::from)
                                .toList();
    }

    private Participation requireParticipation(String userId, String chatId) {
        return participationRepository.findByUserIdAndChatId(userId, chatId)
                                      .orElseThrow(
                                          () -> new NotParticipatingException("참여 중인 채팅이 아닙니다."));
    }

    public void blockUser(String actorUserId, String targetUserId, String chatId,
        int durationInMin) {
        requireChat(chatId);
        Participation actor = requireParticipation(actorUserId, chatId);
        Participation target = requireParticipation(targetUserId, chatId);

        if (!actor.canBlock(target)) {
            throw new UnauthorizedBlockException("차단 권한이 없습니다.");
        }

        validateNotBlocked(target.getId());

        String blockId = idGenerator.generate();
        Block block = Block.blockFor(blockId, target.getId(), durationInMin);
        blockRepository.save(block);
    }

    private void validateNotBlocked(String participationId) {
        Optional<Block> optionalBlock = blockRepository.findActiveBlockByParticipationId(
            participationId, LocalDateTime.now());
        if (optionalBlock.isPresent()) {
            throw new AlreadyBlockedException("차단된 사용자입니다.");
        }
    }
}
