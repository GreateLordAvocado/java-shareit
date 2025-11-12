package ru.practicum.shareit.booking.dto;

public enum BookingState {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED;

    public static BookingState from(String raw) {
        if (raw == null || raw.trim().isEmpty()) return ALL;
        try {
            return BookingState.valueOf(raw.trim().toUpperCase());
        } catch (Exception ex) {
            return ALL;
        }
    }
}
