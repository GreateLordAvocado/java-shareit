package ru.practicum.shareit.gateway.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.gateway.request.dto.ItemRequestCreateDto;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = RequestController.class)
class RequestControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockBean RequestClient client;

    @Test
    void create_should400_whenDescriptionBlank() throws Exception {
        ItemRequestCreateDto bad = new ItemRequestCreateDto();
        bad.setDescription("   ");

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).create(Mockito.anyLong(), Mockito.any());
    }

    @Test
    void create_shouldDelegate_whenValid() throws Exception {
        ItemRequestCreateDto dto = new ItemRequestCreateDto();
        dto.setDescription("нужна дрель");

        Mockito.when(client.create(Mockito.eq(5L), Mockito.any()))
                .thenReturn(ResponseEntity.ok("{}"));

        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 5)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(client, times(1)).create(Mockito.eq(5L), Mockito.any());
    }

    @Test
    void all_should400_whenBadPagination() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());

        Mockito.verify(client, never()).getAll(Mockito.anyLong(), Mockito.anyInt(), Mockito.anyInt());
    }
}
