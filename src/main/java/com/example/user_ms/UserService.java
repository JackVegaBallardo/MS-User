package com.example.user_ms;

import java.util.Map;
import java.util.Optional;

public interface UserService {
    Long ensureUser(String kcIss, String kcSub, Map<String, Object> claims);
    Optional<User> findById(Long id);
}