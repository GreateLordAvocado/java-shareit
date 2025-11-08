package ru.practicum.shareit.booking.dto;

import java.time.LocalDateTime;

public class BookingDto {
    private Long id;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;

    private ItemShort item;
    private UserShort booker;

    public BookingDto() {}

    public BookingDto(Long id, LocalDateTime start, LocalDateTime end,
                      String status, ItemShort item, UserShort booker) {
        this.id = id;
        this.start = start;
        this.end = end;
        this.status = status;
        this.item = item;
        this.booker = booker;
    }

    public static class ItemShort {
        private Long id;
        private String name;

        public ItemShort() {}
        public ItemShort(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public static class UserShort {
        private Long id;
        private String name;

        public UserShort() {}
        public UserShort(Long id, String name) {
            this.id = id;
            this.name = name;
        }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getStart() { return start; }
    public void setStart(LocalDateTime start) { this.start = start; }

    public LocalDateTime getEnd() { return end; }
    public void setEnd(LocalDateTime end) { this.end = end; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public ItemShort getItem() { return item; }
    public void setItem(ItemShort item) { this.item = item; }

    public UserShort getBooker() { return booker; }
    public void setBooker(UserShort booker) { this.booker = booker; }
}
