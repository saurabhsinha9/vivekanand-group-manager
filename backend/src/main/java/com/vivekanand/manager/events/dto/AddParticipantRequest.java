package com.vivekanand.manager.events.dto;


public record AddParticipantRequest(
        Long memberId,
        String memberExternalKey, // email or phone
        String role
) {}

