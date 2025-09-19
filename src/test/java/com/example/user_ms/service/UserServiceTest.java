package com.example.user_ms.service;

import com.example.user_ms.exception.UserNotProvisionedException;
import com.example.user_ms.model.entity.User;
import com.example.user_ms.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserServiceImpl service;


    @Test
    void ensureUserReturnsExistingIdTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "test-user-123";
        User existingUser = createSampleUser();
        Map<String, Object> claims = createSampleClaims();

        when(repo.findByKcIssAndKcSub(kcIss, kcSub)).thenReturn(Optional.of(existingUser));

        // When
        Long result = service.ensureUser(kcIss, kcSub, claims);

        // Then
        assertEquals(1L, result);
        verify(repo, times(1)).findByKcIssAndKcSub(kcIss, kcSub);
        verify(repo, never()).save(any(User.class));
    }

    @Test
    void ensureUserCreatesNewUserTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "new-user-456";
        Map<String, Object> claims = createSampleClaims();
        User savedUser = createSampleUser();
        savedUser.setId(2L);
        savedUser.setKcSub(kcSub);

        when(repo.findByKcIssAndKcSub(kcIss, kcSub)).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenReturn(savedUser);

        // When
        Long result = service.ensureUser(kcIss, kcSub, claims);

        // Then
        assertEquals(2L, result);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();

        assertEquals(kcIss, capturedUser.getKcIss());
        assertEquals(kcSub, capturedUser.getKcSub());
        assertEquals("testuser", capturedUser.getName());
    }

    @Test
    void ensureUserUsesEmailFallbackTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "new-user-456";
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", "test@example.com");

        User savedUser = createSampleUser();
        savedUser.setId(2L);
        savedUser.setName("test@example.com");

        when(repo.findByKcIssAndKcSub(kcIss, kcSub)).thenReturn(Optional.empty());
        when(repo.save(any(User.class))).thenReturn(savedUser);

        // When
        Long result = service.ensureUser(kcIss, kcSub, claims);

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("test@example.com", capturedUser.getName());
    }

    @Test
    void ensureUserHandlesIntegrityViolationTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "test-user-123";
        Map<String, Object> claims = createSampleClaims();
        User existingUser = createSampleUser();

        when(repo.findByKcIssAndKcSub(kcIss, kcSub))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existingUser));

        when(repo.save(any(User.class))).thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // When
        Long result = service.ensureUser(kcIss, kcSub, claims);

        // Then
        assertEquals(1L, result);
        verify(repo, times(2)).findByKcIssAndKcSub(kcIss, kcSub);
        verify(repo, times(1)).save(any(User.class));
    }

    @Test
    void findByIdTest() {
        // Given
        Long userId = 1L;
        User user = createSampleUser();
        when(repo.findById(userId)).thenReturn(Optional.of(user));

        // When
        Optional<User> result = service.findById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(user, result.get());
        verify(repo, times(1)).findById(userId);
    }

    @Test
    void getLocalUserIdOrThrowTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "test-user-123";
        User user = createSampleUser();
        when(repo.findByKcIssAndKcSub(kcIss, kcSub)).thenReturn(Optional.of(user));

        // When
        Long result = service.getLocalUserIdOrThrow(kcIss, kcSub);

        // Then
        assertEquals(1L, result);
        verify(repo, times(1)).findByKcIssAndKcSub(kcIss, kcSub);
    }

    @Test
    void getLocalUserIdOrThrowExceptionTest() {
        // Given
        String kcIss = "http://keycloak:8080/realms/test";
        String kcSub = "nonexistent-user";
        when(repo.findByKcIssAndKcSub(kcIss, kcSub)).thenReturn(Optional.empty());

        // When/Then
        UserNotProvisionedException exception = assertThrows(UserNotProvisionedException.class,
                () -> service.getLocalUserIdOrThrow(kcIss, kcSub));

        assertTrue(exception.getMessage().contains("User with kc_iss=http://keycloak:8080/realms/test kc_sub=nonexistent-user is not provisioned"));

        verify(repo, times(1)).findByKcIssAndKcSub(kcIss, kcSub);
    }

    @Test
    void findAcceptedFriendIdsTest() {
        // Given
        Long userId = 1L;
        List<Long> friendIds = Arrays.asList(2L, 3L, 4L);
        when(repo.findAcceptedFriendIds(userId)).thenReturn(friendIds);

        // When
        List<Long> result = service.findAcceptedFriendIds(userId);

        // Then
        assertEquals(Arrays.asList(2L, 3L, 4L), result);
        verify(repo, times(1)).findAcceptedFriendIds(userId);
    }



    private User createSampleUser() {
        User user = new User();
        user.setId(1L);
        user.setKcIss("http://keycloak:8080/realms/test");
        user.setKcSub("test-user-123");
        user.setName("testuser");
        return user;
    }

    private Map<String, Object> createSampleClaims() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("preferred_username", "testuser");
        claims.put("email", "test@example.com");
        return claims;
    }

}
