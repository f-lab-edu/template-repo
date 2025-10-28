package io.github.jaehyeonhan.project.controller.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageResponse {
    private final String id;
    private final String content;
}
