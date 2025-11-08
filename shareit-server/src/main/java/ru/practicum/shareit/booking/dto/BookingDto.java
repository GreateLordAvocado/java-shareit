package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingDto {
    private Long id;
    private Long itemId;
    private Long bookerId;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;

    public BookingDto() {}

    public BookingDto(Long id, Long itemId, Long bookerId,
                      LocalDateTime start, LocalDateTime end, String status) {
        this.id = id;
        this.itemId = itemId;
        this.bookerId = bookerId;
        this.start = start;
        this.end = end;
        this.status = status;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getItemId() { return itemId; }
    public void setItemId(Long itemId) { this.itemId = itemId; }

    public Long getBookerId() { return bookerId; }
    public void setBookerId(Long bookerId) { this.bookerId = bookerId; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
