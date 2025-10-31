package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.entity.Chat;
import io.github.jaehyeonhan.project.entity.Participation;
import io.github.jaehyeonhan.project.exception.ChatNotFoundException;
import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipationRepository participationRepository;
    private final IdGenerator idGenerator;

    public String create(String userId, String title) {
        String chatId = idGenerator.generate();
        Chat chat = new Chat(chatId, userId, title);
        chatRepository.save(chat);

        String participationId = idGenerator.generate();
        Participation participation = new Participation(participationId, userId, chatId);
        participationRepository.save(participation);

        return chat.getId();
    }

    public void join(String userId, String chatId) {
        requireChat(chatId);

        // 중복 참가 요청은 바로 반환
        Optional<Participation> optParticipation = participationRepository.findByUserIdAndChatId(userId, chatId);
        if(optParticipation.isPresent()) {
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

    }

    public List<MessageDto> getNewMessageList(String userId, String chatId, String lastRead) {
        return null;
    }
}
