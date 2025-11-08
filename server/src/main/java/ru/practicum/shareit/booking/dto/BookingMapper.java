package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.util.Objects;

public final class BookingMapper {
    private BookingMapper() {}

    public static BookingDto toDto(Booking b) {
        Objects.requireNonNull(b, "booking must not be null");

        BookingDto.ItemShort itemShort = null;
        if (b.getItem() != null) {
            itemShort = new BookingDto.ItemShort(
                    b.getItem().getId(),
                    b.getItem().getName()
            );
        }

        BookingDto.UserShort userShort = null;
        if (b.getBooker() != null) {
            userShort = new BookingDto.UserShort(
                    b.getBooker().getId(),
                    b.getBooker().getName()
            );
        }

        BookingDto dto = new BookingDto();
        dto.setId(b.getId());
        dto.setStart(b.getStart());
        dto.setEnd(b.getEnd());
        dto.setStatus(b.getStatus() != null ? b.getStatus().name() : null);
        dto.setItem(itemShort);
        dto.setBooker(userShort);
        return dto;
    }
}
