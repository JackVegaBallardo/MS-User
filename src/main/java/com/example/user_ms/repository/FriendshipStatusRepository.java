package com.example.user_ms.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.user_ms.model.entity.FriendshipStatus;

public interface FriendshipStatusRepository extends JpaRepository<FriendshipStatus, Long>{
        Optional<FriendshipStatus> findByName(String name);

} 