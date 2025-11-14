package ru.practicum.shareit.user.dto;

import lombok.experimental.UtilityClass;
import org.springframework.lang.Nullable;
import ru.practicum.shareit.user.model.User;

@UtilityClass
public class UserMapper {

    @Nullable
    public static UserDto toDto(@Nullable User u) {
        if (u == null) {
            return null;
        }
        return UserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .build();
    }

    @Nullable
    public static User fromDto(@Nullable UserDto d) {
        if (d == null) {
            return null;
        }
        return User.builder()
                .id(d.getId())
                .name(d.getName())
                .email(d.getEmail())
                .build();
    }
}
