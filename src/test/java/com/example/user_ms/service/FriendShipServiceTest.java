package com.example.user_ms.service;

import com.example.user_ms.model.entity.Friendship;
import com.example.user_ms.model.entity.FriendshipStatus;
import com.example.user_ms.model.entity.User;
import com.example.user_ms.repository.FriendshipRepository;
import com.example.user_ms.repository.FriendshipStatusRepository;
import com.example.user_ms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FriendShipServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private FriendshipRepository friendshipRepository;
    @Mock private FriendshipStatusRepository statusRepository;
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @InjectMocks
    private FriendShipServiceImp service;

    private User u1;
    private User u2;
    private FriendshipStatus PENDING;
    private FriendshipStatus ACCEPTED;

    @BeforeEach
    void init() {
        u1 = new User();
        u1.setId(1L);
        u1.setName("Marcos");

        u2 = new User();
        u2.setId(2L);
        u2.setName("Juan");

        PENDING = new FriendshipStatus();
        PENDING.setId(10L);
        PENDING.setName("PENDING");

        ACCEPTED = new FriendshipStatus();
        ACCEPTED.setId(20L);
        ACCEPTED.setName("ACCEPTED");
    }

    @Test
    void acceptCreatesMirrorTest() {
        var original = new Friendship();
        original.setUser(u1);
        original.setFriend(u2);
        original.setStatus(PENDING);

        when(friendshipRepository.findPendingRequest(1L, 2L))
                .thenReturn(Optional.of(original));
        when(statusRepository.findByName("ACCEPTED"))
                .thenReturn(Optional.of(ACCEPTED));
        when(friendshipRepository.findByUser_IdAndFriend_Id(1L, 2L))
                .thenReturn(Optional.empty());

        service.acceptFriendRequest(1L, 2L);

        verify(friendshipRepository, times(2)).saveAndFlush(any(Friendship.class));
        assertEquals("ACCEPTED", original.getStatus().getName());
        assertNotNull(original.getSince());
    }

    @Test
    void acceptUpdatesMirrorTest() {
        var original = new Friendship();
        original.setUser(u1); original.setFriend(u2); original.setStatus(PENDING);

        var mirror = new Friendship();
        mirror.setUser(u2); mirror.setFriend(u1);
        mirror.setStatus(PENDING);

        when(friendshipRepository.findPendingRequest(1L, 2L))
                .thenReturn(Optional.of(original));
        when(statusRepository.findByName("ACCEPTED"))
                .thenReturn(Optional.of(ACCEPTED));
        when(friendshipRepository.findByUser_IdAndFriend_Id(1L, 2L))
                .thenReturn(Optional.of(mirror));

        service.acceptFriendRequest(1L, 2L);

        assertEquals("ACCEPTED", mirror.getStatus().getName());
        assertNotNull(mirror.getSince());
        verify(friendshipRepository, times(2)).saveAndFlush(any(Friendship.class));
    }

    @Test
    void acceptSkipsWhenMirrorAcceptedTest() {
        var original = new Friendship();
        original.setUser(u1); original.setFriend(u2); original.setStatus(PENDING);

        var mirror = new Friendship();
        mirror.setUser(u2); mirror.setFriend(u1);
        mirror.setStatus(ACCEPTED);

        when(friendshipRepository.findPendingRequest(1L, 2L))
                .thenReturn(Optional.of(original));
        when(statusRepository.findByName("ACCEPTED"))
                .thenReturn(Optional.of(ACCEPTED));
        when(friendshipRepository.findByUser_IdAndFriend_Id(1L, 2L))
                .thenReturn(Optional.of(mirror));

        service.acceptFriendRequest(1L, 2L);

        verify(friendshipRepository, times(1)).saveAndFlush(any(Friendship.class));
    }

    @Test
    void acceptFriendRequestExceptionTest() {
        when(friendshipRepository.findPendingRequest(1L, 2L))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> service.acceptFriendRequest(1L, 2L));
        assertTrue(ex.getMessage().contains("Solicitud no encontrada"));
        verify(friendshipRepository, never()).saveAndFlush(any());
    }

    @Test
    void sendFriendRequestExceptionTest() {
        assertThrows(IllegalArgumentException.class,
                () -> service.sendFriendRequest(1L, 1L));
        verifyNoInteractions(statusRepository, friendshipRepository, userRepository);
    }

    @Test
    void sendFriendRequestUpdateExistingRelationTest() {
        var existing = new Friendship();
        existing.setUser(u1); existing.setFriend(u2);
        existing.setStatus(ACCEPTED);

        when(statusRepository.findByName("PENDING")).thenReturn(Optional.of(PENDING));
        when(friendshipRepository.findByUser_IdAndFriend_Id(1L, 2L))
                .thenReturn(Optional.of(existing));

        service.sendFriendRequest(1L, 2L);

        assertEquals("PENDING", existing.getStatus().getName());
        assertNotNull(existing.getSince());
        verify(friendshipRepository).save(existing);
        verify(userRepository, never()).getReferenceById(any());
    }

    @Test
    void sendFriendRequestCreateNewWhenNotExistsTest() {
        when(friendshipRepository.findByUser_IdAndFriend_Id(1L, 2L))
                .thenReturn(Optional.empty());
        when(statusRepository.findByName("PENDING"))
                .thenReturn(Optional.of(PENDING));
        when(userRepository.getReferenceById(1L)).thenReturn(u1);
        when(userRepository.getReferenceById(2L)).thenReturn(u2);

        when(kafkaTemplate.send(eq("user-topic"), anyString()))
                .thenReturn(CompletableFuture.completedFuture(null));

        service.sendFriendRequest(1L, 2L);

        verify(kafkaTemplate).send(eq("user-topic"),
                argThat(payload -> payload.contains(u2.getName())));
        verify(friendshipRepository).save(any(Friendship.class));

    }

    @Test
    void countPendingFriendRequestsTest() {
        when(friendshipRepository.countPendingFriendRequests(1L)).thenReturn(3L);
        long result = service.countPendingFriendRequests(1L);
        assertEquals(3L, result);
    }

    @Test
    void findUsersThatSentPendingRequestsTest() {
        when(friendshipRepository.findUsersThatSentPendingRequests(2L))
                .thenReturn(List.of(u1));
        List<User> list = service.findUsersThatSentPendingRequests(2L);
        assertEquals(1, list.size());
        assertEquals(1L, list.get(0).getId());
    }

    @Test
    void getFriendsTest() {
        when(friendshipRepository.findAcceptedFriends(1L)).thenReturn(List.of(u2));
        List<User> friends = service.getFriends(1L);
        assertEquals(1, friends.size());
        assertEquals(2L, friends.get(0).getId());
    }

    @Test
    void getRequestCandidatesTest() {
        when(friendshipRepository.findUsersYouCanRequest(1L)).thenReturn(List.of(u2));
        List<User> candidates = service.getRequestCandidates(1L);
        assertEquals(1, candidates.size());
        assertEquals(2L, candidates.get(0).getId());
    }

}
