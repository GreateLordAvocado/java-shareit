package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.dto.UserDto;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper om;

    @Test
    void createUser_ok() throws Exception {
        UserDto dto = new UserDto(null, "UserA", "usera@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("UserA")))
                .andExpect(jsonPath("$.email", is("usera@example.com")));
    }

    @Test
    void createUser_invalidEmail() throws Exception {
        UserDto dto = new UserDto(null, "UserB", "bad-email");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Некорректный email")));
    }

    @Test
    void createUser_duplicateEmail() throws Exception {
        UserDto u1 = new UserDto(null, "UserA1", "dup@example.com");
        UserDto u2 = new UserDto(null, "UserB1", "dup@example.com");

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(u1)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(u2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Email уже используется")));
    }

    @Test
    void patchUser_partialUpdate_ok() throws Exception {
        UserDto dto = new UserDto(null, "UserX", "userx@example.com");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isOk());

        UserDto patch = new UserDto(null, "UserX-new", null);
        mockMvc.perform(patch("/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(patch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("UserX-new")))
                .andExpect(jsonPath("$.email", is("userx@example.com")));
    }

    @Test
    void getUser_notFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 42L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString("Пользователь не найден")));
    }

    @Test
    void deleteUser_ok() throws Exception {
        UserDto dto = new UserDto(null, "UserDel", "userdel@example.com");
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(dto))).andExpect(status().isOk());

        mockMvc.perform(delete("/users/{id}", 1L))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/{id}", 1L))
                .andExpect(status().isNotFound());
    }
}
