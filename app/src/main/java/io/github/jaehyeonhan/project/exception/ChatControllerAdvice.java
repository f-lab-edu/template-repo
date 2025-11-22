package io.github.jaehyeonhan.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ChatControllerAdvice {

    @ExceptionHandler
    public ResponseEntity<String> handleAlreadyBlockedException(AlreadyBlockedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedBlockException(
        UnauthorizedBlockException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handleUnauthorizedSendMessageException(
        UnauthorizedSendMessageException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    @ExceptionHandler
    public ResponseEntity<String> handle(InvalidBlockDurationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
