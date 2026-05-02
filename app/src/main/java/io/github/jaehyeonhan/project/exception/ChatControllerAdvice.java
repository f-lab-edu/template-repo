package io.github.jaehyeonhan.project.exception;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChatControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<String> handleChatNotFoundException(ChatNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotParticipatingException(NotParticipatingException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleAlreadyBlockedException(AlreadyBlockedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleNotBlockedException(NotBlockedException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedBlockException(UnauthorizedBlockException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedUnblockException(UnauthorizedUnblockException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedSendMessageException(UnauthorizedSendMessageException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedPromotionException(UnauthorizedPromotionException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidChatTitleException(InvalidChatTitleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidMessageContentException(InvalidMessageContentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleInvalidBlockDurationException(InvalidBlockDurationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleBulkheadFullException(BulkheadFullException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.");
    }
}
