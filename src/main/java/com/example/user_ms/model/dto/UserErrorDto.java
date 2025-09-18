package com.example.user_ms.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class UserErrorDto {

    private int code;
    private String message;
    private LocalDateTime dateTime;
}
