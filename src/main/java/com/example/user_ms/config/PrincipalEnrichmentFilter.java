package com.example.user_ms.config;


import com.example.user_ms.model.dto.CustomUserPrincipal;
import com.example.user_ms.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class PrincipalEnrichmentFilter extends OncePerRequestFilter {

    private final UserService userService;

    public PrincipalEnrichmentFilter(UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {

        var ctx = SecurityContextHolder.getContext();
        var auth = ctx.getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            String iss = jwt.getIssuer() != null ? jwt.getIssuer().toString() : null;
            String sub = jwt.getSubject();
            if (iss == null || sub == null) {
                res.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: missing iss/sub");
                return;
            }

            
            Long userId = userService.ensureUser(iss, sub, Map.copyOf(jwt.getClaims()));

          
            var principal = new CustomUserPrincipal(
                    userId,
                    iss,
                    sub,
                    (String) jwt.getClaims().getOrDefault("preferred_username", null),
                    (String) jwt.getClaims().getOrDefault("email", null)
            );

            
            var newAuth = new UsernamePasswordAuthenticationToken(
                    principal,
                    auth.getCredentials(),
                    auth.getAuthorities()
            );
            newAuth.setDetails(auth.getDetails());
            ctx.setAuthentication(newAuth);
        }

        chain.doFilter(req, res);
    }
}