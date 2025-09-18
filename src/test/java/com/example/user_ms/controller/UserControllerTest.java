package com.example.user_ms.controller;
import com.example.user_ms.config.PrincipalEnrichmentFilter;
import com.example.user_ms.config.SecurityConfig;
import com.example.user_ms.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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
    void me_requiresJwt_andReturnsOk() throws Exception {
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
    void me_unauthorizedWithoutJwt() throws Exception {
        mvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meTest_returnsDto() throws Exception {
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
}
