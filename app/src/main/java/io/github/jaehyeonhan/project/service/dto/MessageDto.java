package io.github.jaehyeonhan.project.service.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageDto {
    private final String id;
    private final String content;
}
