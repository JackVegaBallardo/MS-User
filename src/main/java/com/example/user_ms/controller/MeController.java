package com.example.user_ms.controller;

import java.util.LinkedHashMap;
import java.util.Map;

import com.example.user_ms.model.dto.CustomUserPrincipal;

import com.example.user_ms.model.dto.UserMeResponseDto;
import com.example.user_ms.model.dto.UserTestResponseDto;
import com.example.user_ms.service.UserService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import static com.example.user_ms.mapper.UserMapper.USER_MAPPER;

@RestController
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    /*@GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal CustomUserPrincipal me) {
        var body = new LinkedHashMap<String, Object>();
        body.put("id", me.userId());
        body.put("username", me.username());
        body.put("email", me.email());
        userService.findById(me.userId()).ifPresent(u -> body.put("Name", u.getName()));

        return body;
    }
     @GetMapping("/me/test")
    public Map<String, Object> test(
            @RequestParam String kcIss,
            @RequestParam String kcSub) {

        Long localId = userService.getLocalUserIdOrThrow(kcIss, kcSub);

        var body = new LinkedHashMap<String, Object>();
        body.put("localUserId", localId);
        body.put("kcIss", kcIss);
        body.put("kcSub", kcSub);
        return body;
    }*/
    @GetMapping("/me")
    public ResponseEntity<UserMeResponseDto> me(@AuthenticationPrincipal CustomUserPrincipal me) {
        UserMeResponseDto dto = USER_MAPPER.toUserMeResponseDto(me);

        userService.findById(me.userId())
                .ifPresent(u -> USER_MAPPER.updateNameFromUser(u, dto));

        return ResponseEntity.ok(dto);
    }
    @GetMapping("/me/test")
    public ResponseEntity<UserTestResponseDto> test(
            @RequestParam String kcIss,
            @RequestParam String kcSub) {

        Long localId = userService.getLocalUserIdOrThrow(kcIss, kcSub);
        return ResponseEntity.ok(UserTestResponseDto.builder()
                .localUserId(localId)
                .kcIss(kcIss)
                .kcSub(kcSub)
                .build());
    }
}