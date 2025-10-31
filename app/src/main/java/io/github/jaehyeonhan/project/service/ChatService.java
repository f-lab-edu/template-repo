package io.github.jaehyeonhan.project.service;

import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import io.github.jaehyeonhan.project.service.dto.MessageDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final ParticipationRepository participationRepository;

    public String create(String userId, String title) {
        return null;
    }

    public void join(String userId, String chatId) {

    }

    public void sendMessage(String userId, String chatId, String content) {

    }

    public List<MessageDto> getNewMessageList(String userId, String chatId, String lastRead) {
        return null;
    }
}
