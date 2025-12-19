package com.vivekanand.manager.events.dto;

public record ParticipantView(   Long id,
                                 Long memberId,
                                 String memberName,
                                 String role) {
}
