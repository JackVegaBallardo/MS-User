package com.example.user_ms.controller.advice;

import com.example.user_ms.exception.UserNotProvisionedException;
import com.example.user_ms.model.dto.UserErrorDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotProvisionedException.class)
    public ResponseEntity<?> handleUserNotProvisionedException(UserNotProvisionedException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(UserErrorDto.builder()
                .code(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .dateTime(LocalDateTime.now())
                .build());
    }
}
