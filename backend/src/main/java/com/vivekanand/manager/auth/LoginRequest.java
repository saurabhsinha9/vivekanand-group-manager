
package com.vivekanand.manager.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
class LoginRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
}
