package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingCreateRequest(
        @NotNull Long itemId,
        @NotNull LocalDateTime start,
        @NotNull LocalDateTime end
) {
    public BookingCreateRequest {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new IllegalArgumentException("Дата окончания должна быть позже даты начала");
        }
    }
}