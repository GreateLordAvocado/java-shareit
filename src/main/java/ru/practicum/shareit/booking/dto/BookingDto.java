package ru.practicum.shareit.booking.dto;

import lombok.*;
import ru.practicum.shareit.booking.model.BookingStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;

    private ItemShort item;
    private UserShort booker;

    private BookingStatus status;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ItemShort {
        private Long id;
        private String name;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UserShort {
        private Long id;
        private String name;
    }
}
