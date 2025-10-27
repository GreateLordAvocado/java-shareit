package ru.practicum.shareit.booking.dto;

import ru.practicum.shareit.booking.model.Booking;

public class BookingMapper {

    public static BookingDto toDto(Booking b) {
        if (b == null) {
            return null;
        }
        return BookingDto.builder()
                .id(b.getId())
                .start(b.getStart())
                .end(b.getEnd())
                .status(b.getStatus())
                .item(b.getItem() == null ? null :
                        BookingDto.ItemShort.builder()
                                .id(b.getItem().getId())
                                .name(b.getItem().getName())
                                .build())
                .booker(b.getBooker() == null ? null :
                        BookingDto.UserShort.builder()
                                .id(b.getBooker().getId())
                                .name(b.getBooker().getName())
                                .build())
                .build();
    }
}
