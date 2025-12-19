
package com.vivekanand.manager.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public AuthService(AuthenticationManager am, JwtUtil jwt, UserRepository repo, PasswordEncoder enc) {
        this.authManager = am;
        this.jwtUtil = jwt;
        this.repo = repo;
        this.encoder = enc;
    }


    public LoginResponse login(String username, String password) {
        var user = repo.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        if (user.getLockUntil() != null && user.getLockUntil().isAfter(java.time.Instant.now())) {
            throw new IllegalStateException("Account locked. Try later.");
        }
        try {
            authManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            user.setFailedLoginAttempts(0);
            repo.save(user);
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            return new LoginResponse(token, user.getRole().name(), user.getUsername());
        } catch (org.springframework.security.core.AuthenticationException ex) {
            int attempts = (user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts()) + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= 10) { // configurable
                user.setLockUntil(java.time.Instant.now().plus(java.time.Duration.ofMinutes(30)));
            }
            repo.save(user);
            throw ex;
        }
    }

    public User register(String username, String email, String rawPassword, Role role) {
        if (repo.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");
        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPasswordHash(encoder.encode(rawPassword));
        u.setRole(role);
        return repo.save(u);
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        var u = repo.findByUsername(username).orElseThrow();
        if (!encoder.matches(oldPassword, u.getPasswordHash()))
            throw new IllegalArgumentException("Old password wrong");
        u.setPasswordHash(encoder.encode(newPassword));
        repo.save(u);
    }
}
