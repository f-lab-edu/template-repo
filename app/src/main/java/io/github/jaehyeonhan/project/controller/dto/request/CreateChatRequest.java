package io.github.jaehyeonhan.project.controller.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class CreateChatRequest {

    private final String title;
}
