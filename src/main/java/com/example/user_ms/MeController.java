package com.example.user_ms;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Map<String, Object> me(@AuthenticationPrincipal CustomUserPrincipal me) {
        var body = new LinkedHashMap<String, Object>();
        body.put("id", me.userId());
        body.put("username", me.username());
        body.put("email", me.email());
        userService.findById(me.userId()).ifPresent(u -> body.put("Name", u.getName()));

        return body;
    }
}