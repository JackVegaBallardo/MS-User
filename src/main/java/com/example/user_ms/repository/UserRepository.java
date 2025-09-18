package com.example.user_ms.repository;


import com.example.user_ms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKcIssAndKcSub(String kcIss, String kcSub);
    boolean existsByKcIssAndKcSub(String kcIss, String kcSub);
}