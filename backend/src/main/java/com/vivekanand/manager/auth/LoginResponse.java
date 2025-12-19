package com.vivekanand.manager.auth;

import com.vivekanand.manager.auth.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
class LoginResponse {
    private String token;
    private String role;
    private String username;

    public LoginResponse(String t, String r, String u) {
        token = t;
        role = r;
        username = u;
    }
}

