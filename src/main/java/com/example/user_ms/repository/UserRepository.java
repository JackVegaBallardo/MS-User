package com.example.user_ms.repository;


import com.example.user_ms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByKcIssAndKcSub(String kcIss, String kcSub);
    boolean existsByKcIssAndKcSub(String kcIss, String kcSub);
    
    @Query("""
        SELECT u.id
        FROM User u
        WHERE u.id <> :userId
            AND EXISTS (
            SELECT f.id
            FROM Friendship f
            WHERE f.status.name = 'ACCEPTED'
                AND (
                    (f.user.id = :userId AND f.friend.id = u.id)
                OR (f.friend.id = :userId AND f.user.id = u.id)
                )
            )
        """)
    List<Long> findAcceptedFriendIds(@Param("userId") Long userId);
}