package com.example.user_ms.controller;
import com.example.user_ms.config.PrincipalEnrichmentFilter;
import com.example.user_ms.config.SecurityConfig;
import com.example.user_ms.exception.UserNotProvisionedException;
import com.example.user_ms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@WebMvcTest(MeController.class)
@Import({PrincipalEnrichmentFilter.class, SecurityConfig.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private UserService userService;

    @Test
    void meRequiresJwtTest() throws Exception {
        when(userService.ensureUser(any(), any(), any())).thenReturn(11L);

        mvc.perform(get("/me")
                        .with(jwt().jwt(j -> j
                                .claim("iss", "https://kc/realms/master")
                                .subject("sub-1")
                                .claim("preferred_username", "bob")
                                .claim("email", "b@b.com"))))
                .andExpect(status().isOk());
    }

    @Test
    void meUnauthorizedTest() throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturnsDtoTest() throws Exception {
        when(userService.getLocalUserIdOrThrow("https://kc/realms/master", "sub-xyz"))
                .thenReturn(42L);

        mvc.perform(get("/me/test")
                        .param("kcIss", "https://kc/realms/master")
                        .param("kcSub", "sub-xyz")
                        .with(jwt().jwt(j -> j
                                .claim("iss", "https://kc/realms/master")
                                .subject("sub-xyz"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.localUserId").value(42))
                .andExpect(jsonPath("$.kcIss").value("https://kc/realms/master"))
                .andExpect(jsonPath("$.kcSub").value("sub-xyz"));
    }

    @Test
    void meNotProvisioned404Test() throws Exception {
        when(userService.getLocalUserIdOrThrow("https://kc/realms/master", "sub-xyz"))
                .thenThrow(new UserNotProvisionedException("Usuario no provisionado"));

        mvc.perform(get("/me/test")
                        .param("kcIss", "https://kc/realms/master")
                        .param("kcSub", "sub-xyz")
                        .with(jwt().jwt(j -> j
                                .claim("iss", "https://kc/realms/master")
                                .subject("sub-xyz"))))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").value("Usuario no provisionado"))
                .andExpect(jsonPath("$.dateTime").exists());

        verify(userService).getLocalUserIdOrThrow("https://kc/realms/master", "sub-xyz");
    }

    @Test
    void getFriendIdsTest() throws Exception {
        when(userService.ensureUser(any(), any(), any())).thenReturn(99L);

        when(userService.findAcceptedFriendIds(99L)).thenReturn(List.of(2L, 3L, 5L));

        mvc.perform(get("/me/friends/ids")
                        .with(jwt().jwt(j -> j
                                .claim("iss", "https://kc/realms/master")
                                .subject("sub-1")
                                .claim("preferred_username", "bob")
                                .claim("email", "b@b.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0]").value(2))
                .andExpect(jsonPath("$[1]").value(3))
                .andExpect(jsonPath("$[2]").value(5));

        verify(userService).findAcceptedFriendIds(99L);
    }
}
