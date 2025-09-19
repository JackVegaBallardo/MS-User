package com.example.user_ms.controller;

import com.example.user_ms.model.dto.CustomUserPrincipal;
import com.example.user_ms.model.entity.User;
import com.example.user_ms.service.FriendshipService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendshipController.class)
@AutoConfigureMockMvc
public class FriendShipControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendshipService friendshipService;

    private Authentication buildAuth(long id) {
        CustomUserPrincipal me = new CustomUserPrincipal(
                id,
                "iss-test",
                "sub-test",
                "raul",
                "r@x.com"
        );

        return new UsernamePasswordAuthenticationToken(me, null, List.of());
    }

    @Test
    void countPendingFriendRequestsTest() throws Exception {
        when(friendshipService.countPendingFriendRequests(1L)).thenReturn(5L);

        mockMvc.perform(get("/api/friendships/pending")
                        .with(authentication(buildAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(friendshipService).countPendingFriendRequests(1L);
    }

    @Test
    void findUsersThatSentPendingRequestsTest() throws Exception {
        User u1 = new User();
        u1.setId(10L);
        User u2 = new User();
        u2.setId(11L);
        when(friendshipService.findUsersThatSentPendingRequests(1L))
                .thenReturn(List.of(u1, u2));

        mockMvc.perform(get("/api/friendships/pending/users")
                        .with(authentication(buildAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(10));

        verify(friendshipService).findUsersThatSentPendingRequests(1L);
    }

    @Test
    void acceptFriendRequestTest() throws Exception {
        mockMvc.perform(put("/api/friendships/accept")
                        .param("friendId", "2")
                        .with(authentication(buildAuth(1L)))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(friendshipService).acceptFriendRequest(1L, 2L);
    }

    @Test
    void getFriendsTest() throws Exception {
        User u1 = new User(); u1.setId(2L);
        when(friendshipService.getFriends(1L)).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/friendships/Myfriends")
                        .with(authentication(buildAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2));

        verify(friendshipService).getFriends(1L);
    }

    @Test
    void sendFriendRequestTest() throws Exception {
        mockMvc.perform(post("/api/friendships/request")
                        .param("friendId", "2")
                        .with(authentication(buildAuth(1L)))
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(friendshipService).sendFriendRequest(1L, 2L);
    }

    @Test
    void getRequestCandidatesTest() throws Exception {
        User u1 = new User(); u1.setId(3L);
        when(friendshipService.getRequestCandidates(1L)).thenReturn(List.of(u1));

        mockMvc.perform(get("/api/friendships/candidates")
                        .with(authentication(buildAuth(1L))))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3));

        verify(friendshipService).getRequestCandidates(1L);
    }
}
