package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Message;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.exception.NotParticipatingException;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipationRepository participationRepository;
    private final MessageRepository messageRepository;
    private final IdGenerator idGenerator;

    public String createChat(String userId, String title) {
        String chatId = idGenerator.generate();
        Chat chat = new Chat(chatId, userId, title);
        chatRepository.save(chat);

        join(userId, chatId);

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
        Participation participation = new Participation(participationId, userId, chatId);
        participationRepository.save(participation);
    }

    private void requireChat(String chatId) {
        chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("채팅이 없습니다."));
    }

    public void sendMessage(String userId, String chatId, String content) {
        requireParticipation(userId, chatId);

        String messageId = idGenerator.generate();
        Message message = new Message(messageId, chatId, userId, content);
        messageRepository.save(message);
    }

    public List<MessageDto> getMessageList(String userId, String chatId, LocalDateTime lastRead) {
        requireParticipation(userId, chatId);

        return messageRepository.findMessagesAfterLastRead(userId, chatId, lastRead).stream()
                .map(MessageDto::from)
                .toList();
    }

    private void requireParticipation(String userId, String chatId) {
        participationRepository.findByUserIdAndChatId(userId, chatId)
                               .orElseThrow(() -> new NotParticipatingException("참여 중인 채팅이 아닙니다."));
    }
}
