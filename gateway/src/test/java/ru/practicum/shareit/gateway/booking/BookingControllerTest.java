package ru.practicum.shareit.gateway.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.booking.dto.BookingCreateDto;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String HDR = "X-Sharer-User-Id";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingClient client;

    @Nested
    class Create {
        @Test
        @DisplayName("POST /bookings — 400 без itemId")
        void create_noItemId() throws Exception {
            BookingCreateDto dto = new BookingCreateDto();
            dto.setStart(LocalDateTime.now().plusHours(1));
            dto.setEnd(LocalDateTime.now().plusHours(2));

            mvc.perform(post("/bookings")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }

        @Test
        @DisplayName("POST /bookings — 400 end<=start")
        void create_badDates() throws Exception {
            BookingCreateDto dto = new BookingCreateDto();
            dto.setItemId(10L);
            dto.setStart(LocalDateTime.now().plusHours(2));
            dto.setEnd(LocalDateTime.now().plusHours(1));

            mvc.perform(post("/bookings")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Дата окончания должна быть позже даты начала"));
        }

        @Test
        @DisplayName("POST /bookings — 201 OK")
        void create_ok() throws Exception {
            BookingCreateDto dto = new BookingCreateDto();
            dto.setItemId(10L);
            dto.setStart(LocalDateTime.now().plusHours(1));
            dto.setEnd(LocalDateTime.now().plusHours(2));

            Mockito.when(client.create(anyLong(), any()))
                    .thenReturn(ResponseEntity.status(201).body("{\"id\":77}"));

            mvc.perform(post("/bookings")
                            .header(HDR, 1L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("POST /bookings — 400 без заголовка X-Sharer-User-Id")
        void create_noHeader() throws Exception {
            BookingCreateDto dto = new BookingCreateDto();
            dto.setItemId(10L);
            dto.setStart(LocalDateTime.now().plusHours(1));
            dto.setEnd(LocalDateTime.now().plusHours(2));

            mvc.perform(post("/bookings")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(om.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    class Approve {
        @Test
        @DisplayName("PATCH /bookings/{id}?approved=true — 200 OK")
        void approve_ok() throws Exception {
            Mockito.when(client.approve(anyLong(), anyLong(), anyBoolean()))
                    .thenReturn(ResponseEntity.ok("{\"id\":1,\"status\":\"APPROVED\"}"));

            mvc.perform(patch("/bookings/{id}", 5L)
                            .header(HDR, 2L)
                            .param("approved", "true"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("PATCH /bookings/{id} без заголовка — 400")
        void approve_noHeader() throws Exception {
            mvc.perform(patch("/bookings/{id}", 5L)
                            .param("approved", "true"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    class GetById {
        @Test
        @DisplayName("GET /bookings/{id} — 200 OK")
        void get_ok() throws Exception {
            Mockito.when(client.getById(anyLong(), anyLong()))
                    .thenReturn(ResponseEntity.ok("{\"id\":5}"));

            mvc.perform(get("/bookings/{bookingId}", 5L)
                            .header(HDR, 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /bookings/{id} без заголовка — 400")
        void get_noHeader() throws Exception {
            mvc.perform(get("/bookings/{bookingId}", 5L))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    class Lists {
        @Test
        @DisplayName("GET /bookings?state=UNKNOWN — 400 Unknown state")
        void forBooker_unknownState() throws Exception {
            mvc.perform(get("/bookings")
                            .header(HDR, 1L)
                            .param("state", "unknown"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Unknown state: unknown"));
        }

        @Test
        @DisplayName("GET /bookings — 200 OK (без state)")
        void forBooker_ok() throws Exception {
            Mockito.when(client.forBooker(anyLong(), isNull()))
                    .thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/bookings")
                            .header(HDR, 1L))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /bookings/owner?state=WAITING — 200 OK")
        void forOwner_ok() throws Exception {
            Mockito.when(client.forOwner(anyLong(), eq("WAITING")))
                    .thenReturn(ResponseEntity.ok("[]"));

            mvc.perform(get("/bookings/owner")
                            .header(HDR, 1L)
                            .param("state", "WAITING"))
                    .andExpect(status().isOk());
        }
    }
}
