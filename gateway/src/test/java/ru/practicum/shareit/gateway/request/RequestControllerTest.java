package ru.practicum.shareit.gateway.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RequestController.class)
class RequestControllerTest {

    @Autowired private MockMvc mvc;
    @MockBean private RequestClient client;

    @Test
    @DisplayName("POST /requests: без заголовка — 400")
    void create_missingHeader() throws Exception {
        mvc.perform(post("/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /requests: пустое описание — 400")
    void create_emptyDescription() throws Exception {
        mvc.perform(post("/requests")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /requests/all: неверные from/size — 400")
    void getAll_badPaging() throws Exception {
        mvc.perform(get("/requests/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1").param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("from >= 0")));
    }

    @Test
    @DisplayName("GET /requests: happy path — 200")
    void getOwn_ok() throws Exception {
        Mockito.when(client.getOwn(1L)).thenReturn(ResponseEntity.ok("[]"));
        mvc.perform(get("/requests").header("X-Sharer-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
