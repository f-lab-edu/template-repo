package io.github.jaehyeonhan.project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.containers.GenericContainer;

@TestConfiguration
public class RedisTestContainerConfig {

    static final GenericContainer<?> REDIS =
        new GenericContainer<>("redis:7").withExposedPorts(6379);

    static {
        REDIS.start();
    }

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(REDIS.getHost(), REDIS.getMappedPort(6379));
    }
}
