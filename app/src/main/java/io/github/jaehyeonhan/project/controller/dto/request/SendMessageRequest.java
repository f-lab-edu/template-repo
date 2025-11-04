package io.github.jaehyeonhan.project.controller.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SendMessageRequest {

    private final String userId;
    private final String content;
}
