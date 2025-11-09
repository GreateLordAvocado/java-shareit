package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingCreateRequest;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingState;
import ru.practicum.shareit.booking.service.BookingService;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean BookingService service;

    private static final String HDR = "X-Sharer-User-Id";

    @Test
    void create_should400_whenEndNotAfterStart() throws Exception {
        BookingCreateRequest bad = new BookingCreateRequest();
        bad.setItemId(1L);
        bad.setStart(LocalDateTime.now().plusDays(1));
        bad.setEnd(LocalDateTime.now());

        mvc.perform(post("/bookings")
                        .header(HDR, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(service, never()).create(anyLong(), any(BookingCreateRequest.class));
    }

    @Test
    void create_shouldCallService_whenValidInput() throws Exception {
        BookingCreateRequest ok = new BookingCreateRequest();
        ok.setItemId(1L);
        ok.setStart(LocalDateTime.now().plusHours(1));
        ok.setEnd(LocalDateTime.now().plusHours(2));

        BookingDto stub = new BookingDto();
        Mockito.when(service.create(eq(7L), any(BookingCreateRequest.class))).thenReturn(stub);

        mvc.perform(post("/bookings")
                        .header(HDR, 7L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(ok)))
                .andExpect(status().isOk());

        Mockito.verify(service, times(1)).create(eq(7L), any(BookingCreateRequest.class));
    }

    @Test
    void getForBooker_should400_whenUnknownState() throws Exception {
        mvc.perform(get("/bookings")
                        .header(HDR, 1L)
                        .param("state", "WAT_IS_THAT"))
                .andExpect(status().isBadRequest());

        Mockito.verify(service, never()).findByBooker(anyLong(), any());
    }

    @Test
    void getForBooker_shouldCallService_whenValidState() throws Exception {
        Mockito.when(service.findByBooker(eq(3L), eq(BookingState.FUTURE)))
                .thenReturn(Collections.emptyList());

        mvc.perform(get("/bookings")
                        .header(HDR, 3L)
                        .param("state", "future")) // контроллер нормализует в FUTURE
                .andExpect(status().isOk());

        Mockito.verify(service, times(1)).findByBooker(3L, BookingState.FUTURE);
    }

    @Test
    void approve_shouldCallService() throws Exception {
        BookingDto stub = new BookingDto();
        Mockito.when(service.approve(eq(5L), eq(42L), eq(true))).thenReturn(stub);

        mvc.perform(patch("/bookings/{id}", 42L)
                        .header(HDR, 5L)
                        .param("approved", "true"))
                .andExpect(status().isOk());

        Mockito.verify(service, times(1)).approve(5L, 42L, true);
    }
}
