package com.vivekanand.manager.members.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateMemberRequest(
        @NotBlank String fullName,
        @Email String email,
        String phone,
        String address,
        Boolean notifyEmail,
        Boolean notifyWhatsapp
) {
}
