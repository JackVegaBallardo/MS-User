package com.example.user_ms.service;

import com.example.user_ms.model.entity.User;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FriendshipService {
    long countPendingFriendRequests(long UserId);
    List<User> findUsersThatSentPendingRequests(Long userId);
    void acceptFriendRequest(Long userId, Long friendId);
    List<User> getFriends(Long userId);
    void sendFriendRequest(Long senderId, Long receiverId);
    List<User> getRequestCandidates(Long userId);
}
