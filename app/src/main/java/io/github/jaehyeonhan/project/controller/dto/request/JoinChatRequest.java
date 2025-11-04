package io.github.jaehyeonhan.project.controller.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JoinChatRequest {
    private final String userId;
}
