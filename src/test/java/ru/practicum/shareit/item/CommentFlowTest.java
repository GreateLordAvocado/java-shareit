package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.item.dto.CommentCreateDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CommentFlowTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    private long createUser(String name, String email) throws Exception {
        String uniqueEmail = email.replace("@", "+" + System.nanoTime() + "@");

        UserDto u = new UserDto(null, name, uniqueEmail);
        String body = om.writeValueAsString(u);

        var result = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(r -> {
                    int s = r.getResponse().getStatus();
                    if (s != 200 && s != 201) {
                        throw new AssertionError("Expected 200 or 201, got " + s);
                    }
                })
                .andReturn();

        String json = result.getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    private long createItem(long ownerId, String name) throws Exception {
        var dto = new ItemDto(null, name, "desc", true, null, null, null, null);
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
                .andReturn().getResponse().getContentAsString();
        return om.readTree(json).get("id").asLong();
    }

    @Test
    void addComment_onlyAfterApprovedPastBooking() throws Exception {
        long owner  = createUser("Owner", "owner@ex.com");
        long booker = createUser("Booker", "booker@ex.com");
        long itemId = createItem(owner, "Триммер");

        var start = LocalDateTime.now().minusDays(2);
        var end   = LocalDateTime.now().minusDays(1);
        long bookingId = createBooking(booker, itemId, start, end);

        mockMvc.perform(patch("/bookings/{id}", bookingId)
                        .param("approved", "true")
                        .header(HDR, owner))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("APPROVED")));

        var body = new CommentCreateDto();
        body.setText("Отличная вещь!");
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HDR, booker)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text", is("Отличная вещь!")))
                .andExpect(jsonPath("$.authorName", is("Booker")));

        long stranger = createUser("Stranger", "str@ex.com");
        var bad = new CommentCreateDto();
        bad.setText("Я не брал, но напишу :)");
        mockMvc.perform(post("/items/{itemId}/comment", itemId)
                        .header(HDR, stranger)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Комментировать вещь может только пользователь")));
    }
}
