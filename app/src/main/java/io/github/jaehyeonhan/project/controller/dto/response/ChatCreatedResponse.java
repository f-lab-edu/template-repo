package io.github.jaehyeonhan.project.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ChatCreatedResponse {
    private final String chatId;
}
