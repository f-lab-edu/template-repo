package io.github.jaehyeonhan.project.controller.dto.request;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BlockUserRequest {

    private final String requesterUserId;
    private final String targetUserId;

    private final int duration;
}
