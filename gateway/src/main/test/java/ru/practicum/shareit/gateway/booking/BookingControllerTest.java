package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingClient client;

    @Test
    void create_should400_whenEndNotAfterStart() throws Exception {
        BookingCreateDto bad = new BookingCreateDto();
        bad.setItemId(1L);
        bad.setStart(LocalDateTime.now().plusDays(1));
        bad.setEnd(LocalDateTime.now());

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void create_shouldDelegate_whenValid() throws Exception {
        BookingCreateDto ok = new BookingCreateDto();
        ok.setItemId(1L);
        ok.setStart(LocalDateTime.now().plusHours(1));
        ok.setEnd(LocalDateTime.now().plusHours(2));

        Mockito.when(client.create(Mockito.eq(7L), ArgumentMatchers.any()))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(post("/bookings")
                        .header("X-Sharer-User-Id", 7)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(ok)))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).create(Mockito.eq(7L), ArgumentMatchers.any());
    }

    @Test
    void listForBooker_should400_whenUnknownState() throws Exception {
        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "WAT_IS_THAT"))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).forBooker(Mockito.anyLong(), Mockito.anyString());
    }

    @Test
    void listForBooker_shouldPassNormalizedState() throws Exception {
        Mockito.when(client.forBooker(Mockito.eq(3L), Mockito.eq("FUTURE")))
                .thenReturn(ResponseEntity.ok("[]"));

        mvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 3)
                        .param("state", "future"))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).forBooker(3L, "FUTURE");
    }

    @Test
    void approve_shouldDelegate() throws Exception {
        Mockito.when(client.approve(Mockito.eq(5L), Mockito.eq(42L), Mockito.eq(true)))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(patch("/bookings/{id}", 42L)
                        .header("X-Sharer-User-Id", 5)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).approve(5L, 42L, true);
    }
}
