package ru.practicum.shareit.request.dto;

import jakarta.validation.constraints.NotBlank;

public class ItemRequestCreateDto {

    @NotBlank
    private String description;

    public ItemRequestCreateDto() {
    }

    public ItemRequestCreateDto(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
