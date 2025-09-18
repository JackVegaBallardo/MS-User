package com.example.user_ms.model.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UserTestResponseDto {

    private Long localUserId;
    private String kcIss;
    private String kcSub;
}
