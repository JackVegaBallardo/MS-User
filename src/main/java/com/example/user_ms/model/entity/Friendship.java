package com.example.user_ms.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@Entity
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id")
    private User friend;

    private LocalDateTime since;

    // PENDING, ACCEPTED, BLOCKED, etc.
    @ManyToOne
    @JoinColumn(name = "status_id", nullable = false)
    private FriendshipStatus status;
}
