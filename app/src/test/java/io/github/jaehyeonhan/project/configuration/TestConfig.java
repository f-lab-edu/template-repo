package io.github.jaehyeonhan.project.configuration;

import io.github.jaehyeonhan.project.repository.ChatRepository;
import io.github.jaehyeonhan.project.repository.MemoryChatRepository;
import io.github.jaehyeonhan.project.repository.MemoryMessageRepository;
import io.github.jaehyeonhan.project.repository.MemoryParticipationRepository;
import io.github.jaehyeonhan.project.repository.MessageRepository;
import io.github.jaehyeonhan.project.repository.ParticipationRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestConfig {

    @Primary
    @Bean
    public ChatRepository testChatRepository() {
        return new MemoryChatRepository();
    }

    @Primary
    @Bean
    public ParticipationRepository testParticipationRepository() {
        return new MemoryParticipationRepository();
    }

    @Primary
    @Bean
    public MessageRepository testMessageRepository() {
        return new MemoryMessageRepository();
    }
}
