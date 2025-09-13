package com.example.user_ms;

import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public Long ensureUser(String kcIss, String kcSub, Map<String,Object> claims) {
        var existing = repo.findByKcIssAndKcSub(kcIss, kcSub);
        if (existing.isPresent()) return existing.get().getId();

        try {
            var u = new User();
            u.setKcIss(kcIss);
            u.setKcSub(kcSub);
            var preferred = (String) claims.getOrDefault("preferred_username", null);
            var email = (String) claims.getOrDefault("email", null);
            u.setName(preferred != null ? preferred : email);
            return repo.save(u).getId();
        } catch (DataIntegrityViolationException e) {
            return repo.findByKcIssAndKcSub(kcIss, kcSub).orElseThrow().getId();
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

}
