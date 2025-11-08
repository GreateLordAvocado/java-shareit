package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.util.Objects;

public final class BookingMapper {
    private BookingMapper() {}

    public static BookingDto toDto(Booking b) {
        Objects.requireNonNull(b, "booking must not be null");

        Long itemId   = (b.getItem()   != null) ? b.getItem().getId()   : null;
        Long bookerId = (b.getBooker() != null) ? b.getBooker().getId() : null;

        BookingDto dto = new BookingDto();
        dto.setId(b.getId());
        dto.setItemId(itemId);
        dto.setBookerId(bookerId);
        dto.setStart(b.getStart());
        dto.setEnd(b.getEnd());
        dto.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
        return dto;
    }
}
