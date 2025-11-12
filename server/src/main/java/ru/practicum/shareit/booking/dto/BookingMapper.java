package ru.practicum.shareit.booking.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.model.Booking;

import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BookingMapper {

    public static BookingDto toDto(Booking b) {
        Objects.requireNonNull(b, "booking must not be null");

        BookingDto.ItemShort itemShort = null;
        if (b.getItem() != null) {
            itemShort = BookingDto.ItemShort.builder()
                    .id(b.getItem().getId())
                    .name(b.getItem().getName())
                    .build();
        }

        BookingDto.UserShort userShort = null;
        if (b.getBooker() != null) {
            userShort = BookingDto.UserShort.builder()
                    .id(b.getBooker().getId())
                    .name(b.getBooker().getName())
                    .build();
        }

        return BookingDto.builder()
                .id(b.getId())
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus())
                .item(itemShort)
                .booker(userShort)
                .build();
    }
}
