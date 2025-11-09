package ru.practicum.shareit.gateway.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserUpdateDto {

    @Size(max = 255, message = "Длина имени не должна превышать 255 символов")
    @Pattern(regexp = ".*\\S.*", message = "Имя не может быть пустым", flags = Pattern.Flag.UNICODE_CASE)
    private String name;

    @Email(message = "Некорректный email")
    @Size(max = 320, message = "Длина email не должна превышать 320 символов")
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
