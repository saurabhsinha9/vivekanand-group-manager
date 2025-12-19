
package com.vivekanand.manager.auth;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService s) {
        this.authService = s;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.getUsername(), req.getPassword()));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req.getUsername(), req.getEmail(), req.getPassword(), req.getRole()));
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<Void> change(@RequestBody ChangePasswordRequest req, Authentication auth) {
        authService.changePassword(auth.getName(), req.getOldPassword(), req.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
