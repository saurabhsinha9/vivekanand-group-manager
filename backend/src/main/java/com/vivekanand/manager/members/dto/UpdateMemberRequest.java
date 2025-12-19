package com.vivekanand.manager.members.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateMemberRequest(
        @NotBlank String fullName,
        @Email String email,
        String phone,
        String address,
        Boolean active,
        Boolean notifyEmail,
        Boolean notifyWhatsapp
) {}
