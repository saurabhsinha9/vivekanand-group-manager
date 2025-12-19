package com.vivekanand.manager.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record EventUpdateRequest(
        @NotBlank String name,
        String description,
        String location,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        LocalTime startTime,   // optional
        LocalTime endTime      // optional
) {
}