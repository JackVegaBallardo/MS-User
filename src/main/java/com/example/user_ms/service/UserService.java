package com.example.user_ms.service;

import java.util.Map;
import java.util.Optional;

import com.example.user_ms.model.entity.User;
import org.springframework.data.jpa.repository.Query;

public interface UserService {
    Long ensureUser(String kcIss, String kcSub, Map<String, Object> claims);
    Optional<User> findById(Long id);
    @Query("select u.id from User u where u.kcIss = :kcIss and u.kcSub = :kcSub")
    Long getLocalUserIdOrThrow(String kcIss, String kcSub);
}