package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.exceptions.ValidationException;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String raw) {
        if (raw == null || raw.isBlank()) {
            return ALL;
        }
        try {
            return BookingState.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Неизвестный state: " + raw);
        }
    }
}