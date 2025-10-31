package io.github.jaehyeonhan.project.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class Chat {

    private String id;
    private String creatorId;

    @Setter
    private String title;

}
