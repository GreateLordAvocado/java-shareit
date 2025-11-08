package ru.practicum.shareit.gateway.user.dto;

import jakarta.validation.constraints.Email;

public class UserUpdateDto {

    private String name;

    @Email(message = "Некорректный email")
    private String email;

    public UserUpdateDto() {}

    public UserUpdateDto(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
