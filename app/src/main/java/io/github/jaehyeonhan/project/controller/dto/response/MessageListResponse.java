package io.github.jaehyeonhan.project.controller.dto.response;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageListResponse {
    private final List<MessageResponse> messages;
}
