package ru.practicum.shareit.gateway.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.gateway.user.dto.UserCreateDto;
import ru.practicum.shareit.gateway.user.dto.UserUpdateDto;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserClient client;

    @PostMapping
    public ResponseEntity<String> create(@Valid @RequestBody UserCreateDto dto) {
        log.debug("POST /users, dto={}", dto);
        return client.create(dto);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> update(@PathVariable Long id,
                                         @Valid @RequestBody UserUpdateDto dto) {
        log.debug("PATCH /users/{}, dto={}", id, dto);

        if (dto.getName() == null && dto.getEmail() == null) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\":\"Body must contain at least one of the fields: name or email\"}");
        }
        return client.patch(id, dto);
    }

    @GetMapping
    public ResponseEntity<String> getAll() {
        log.debug("GET /users");
        return client.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getById(@PathVariable Long id) {
        log.debug("GET /users/{}", id);
        return client.getById(id);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        log.debug("DELETE /users/{}", id);
        return client.delete(id);
    }
}
