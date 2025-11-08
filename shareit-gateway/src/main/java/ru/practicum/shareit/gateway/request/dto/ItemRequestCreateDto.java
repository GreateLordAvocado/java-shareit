package ru.practicum.shareit.gateway.request.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemRequestCreateDto {

    @NotBlank(message = "Описание запроса не должно быть пустым")
    private String description;

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
