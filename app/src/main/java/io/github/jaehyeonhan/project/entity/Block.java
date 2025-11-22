package io.github.jaehyeonhan.project.entity;

import io.github.jaehyeonhan.project.constant.TimeConstant;
import io.github.jaehyeonhan.project.exception.InvalidBlockDurationException;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Block {

    @Id
    private String id;

    private String participationId;

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    private boolean retracted;

    public static Block blockFor(String id, String participationId, int durationInMin) {
        validateDuration(durationInMin);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt;
        if (durationInMin == 0) { // 영구 차단
            expiresAt = TimeConstant.MAX_DATETIME;
        } else {
            expiresAt = now.plusMinutes(durationInMin);
        }

        return new Block(id, participationId, now, expiresAt, false);
    }

    private static void validateDuration(int durationInMin) {
        if ((durationInMin < 5 && durationInMin != 0) || durationInMin > 30) {
            throw new InvalidBlockDurationException(
                "block duartion should be between 5 and 30 minutes");
        }
    }
}
