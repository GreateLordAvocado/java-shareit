package ru.practicum.shareit.gateway.user.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

    @Size(max = 255, message = "Длина имени не должна превышать 255 символов")
    @Pattern(regexp = ".*\\S.*", message = "Имя не может быть пустым", flags = Pattern.Flag.UNICODE_CASE)
    private String name;

    @Email(message = "Некорректный email")
    @Size(max = 320, message = "Длина email не должна превышать 320 символов")
    private String email;
}
