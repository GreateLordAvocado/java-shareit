package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BookingControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private long createUser(String name, String email) throws Exception {
        var u = new UserDto(null, name, email);
        String json = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(u)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    private long createItem(long ownerId, String name, boolean available) throws Exception {
        var dto = new ItemDto(null, name, "desc", available, null, null, null, null);
        String json = mockMvc.perform(post("/items")
                        .header(HDR, ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    private long createBooking(long bookerId, long itemId, LocalDateTime start, LocalDateTime end) throws Exception {
        var req = new BookingCreateRequest();
        req.setItemId(itemId);
        req.setStart(start);
        req.setEnd(end);

        String json = mockMvc.perform(post("/bookings")
                        .header(HDR, bookerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("WAITING")))
                .andReturn().getResponse().getContentAsString();

        return om.readTree(json).get("id").asLong();
    }

    @Test
    void booking_flow_create_approve_getAndLists() throws Exception {
        long owner  = createUser("Owner", "owner@ex.com");
        long booker = createUser("Booker", "booker@ex.com");
        long itemId = createItem(owner, "Лобзик", true);

        var start = LocalDateTime.now().plusHours(1);
        var end   = start.plusHours(2);
        long bookingId = createBooking(booker, itemId, start, end);

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", "true")
                        .header(HDR, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        mockMvc.perform(get("/bookings/{id}", bookingId).header(HDR, booker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int) bookingId)))
                .andExpect(jsonPath("$.item.id", is((int) itemId)))
                .andExpect(jsonPath("$.booker.id", is((int) booker)));

        mockMvc.perform(get("/bookings/{id}", bookingId).header(HDR, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        mockMvc.perform(get("/bookings").header(HDR, booker).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id", is((int) bookingId)));

        mockMvc.perform(get("/bookings/owner").header(HDR, owner).param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].id", is((int) bookingId)));
    }

    @Test
    void booking_reject_flow() throws Exception {
        long owner  = createUser("Owner", "o@ex.com");
        long booker = createUser("Booker", "b@ex.com");
        long itemId = createItem(owner, "Перфоратор", true);

        var start = LocalDateTime.now().plusDays(1);
        var end   = start.plusHours(3);
        long bookingId = createBooking(booker, itemId, start, end);

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", "false")
                        .header(HDR, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("REJECTED")));

        mockMvc.perform(get("/bookings/owner").header(HDR, owner).param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].status", everyItem(is("REJECTED"))));
    }
}
