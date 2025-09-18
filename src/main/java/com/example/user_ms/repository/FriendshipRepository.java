package com.example.user_ms.repository;

import com.example.user_ms.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.user_ms.model.entity.Friendship;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    @Query("SELECT COUNT(f) FROM Friendship f " +
           "WHERE f.friend.id = :userId " +
           "AND f.status.name = 'PENDING'")
    long countPendingFriendRequests(@Param("userId") Long userId);
    @Query("SELECT f.user FROM Friendship f " +
            "WHERE f.friend.id = :userId " +
            "AND f.status.name = 'PENDING'")
    List<User> findUsersThatSentPendingRequests(@Param("userId") Long userId);

    @Query("SELECT f FROM Friendship f " +
       "WHERE f.user.id = :friendId " +
       "AND f.friend.id = :userId " +
       "AND f.status.name = 'PENDING'")
    Optional<Friendship> findPendingRequest(@Param("userId") Long userId,
                                        @Param("friendId") Long friendId);


    boolean existsByUserIdAndFriendId(Long userId, Long friendId);
    Optional<Friendship> findByUser_IdAndFriend_Id(Long userId, Long friendId);

    
    @Query("""
    SELECT u
    FROM User u
    WHERE
    EXISTS (
        SELECT 1 FROM Friendship f
        WHERE f.user.id = :userId
        AND f.friend.id = u.id
        AND f.status.name = 'ACCEPTED'
    )
    OR EXISTS (
        SELECT 1 FROM Friendship f
        WHERE f.friend.id = :userId
        AND f.user.id = u.id
        AND f.status.name = 'ACCEPTED'
    )
    """)
    List<User> findAcceptedFriends(@Param("userId") Long userId);

     @Query("""
       SELECT u
       FROM User u
       WHERE u.id <> :userId
         AND NOT EXISTS (
           SELECT 1 FROM Friendship f
           WHERE ((f.user.id = :userId AND f.friend.id = u.id)
               OR (f.user.id = u.id AND f.friend.id = :userId))
             AND f.status.name = 'ACCEPTED'
         )
         AND NOT EXISTS (
           SELECT 1 FROM Friendship f2
           WHERE ((f2.user.id = :userId AND f2.friend.id = u.id)
               OR (f2.user.id = u.id AND f2.friend.id = :userId))
             AND f2.status.name = 'BLOCKED'
         )
        AND NOT EXISTS (
           SELECT 1 FROM Friendship f2
           WHERE ((f2.user.id = :userId AND f2.friend.id = u.id)
               OR (f2.user.id = u.id AND f2.friend.id = :userId))
             AND f2.status.name = 'PENDING'
         )
    """)
    List<User> findUsersYouCanRequest(@Param("userId") Long userId);
}