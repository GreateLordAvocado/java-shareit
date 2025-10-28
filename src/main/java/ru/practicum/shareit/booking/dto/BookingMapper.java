package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

import java.util.Objects;

public final class BookingMapper {

    private BookingMapper() {
        // utility class
    }

    public static BookingDto toDto(Booking b) {
        Objects.requireNonNull(b, "booking must not be null");

        BookingDto.BookingDtoBuilder builder = BookingDto.builder()
                .id(b.getId())
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus());

        if (b.getItem() != null) {
            builder.item(BookingDto.ItemShort.builder()
                    .id(b.getItem().getId())
                    .name(b.getItem().getName())
                    .build());
        }

        if (b.getBooker() != null) {
            builder.booker(BookingDto.UserShort.builder()
                    .id(b.getBooker().getId())
                    .name(b.getBooker().getName())
                    .build());
        }

        return builder.build();
    }
}
