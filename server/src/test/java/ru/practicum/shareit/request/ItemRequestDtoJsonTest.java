package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {
    @Autowired private ObjectMapper mapper;

    @Test
    void serialize() throws Exception {
        ItemRequestDto.ItemAnswerDto ans = ItemRequestDto.ItemAnswerDto.builder()
                .id(10L).name("Дрель").ownerId(2L).build();
        ItemRequestDto dto = ItemRequestDto.builder()
                .id(1L)
                .description("Нужна дрель")
                .created(LocalDateTime.of(2025,1,1,12,0))
                .items(List.of(ans))
                .build();

        String json = mapper.writeValueAsString(dto);
        assertThat(json).contains("\"id\":1");
        assertThat(json).contains("\"description\":\"Нужна дрель\"");
        assertThat(json).contains("\"items\":[");
    }
}
