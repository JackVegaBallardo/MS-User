package com.example.user_ms;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKcIssAndKcSub(String kcIss, String kcSub);
    boolean existsByKcIssAndKcSub(String kcIss, String kcSub);
}