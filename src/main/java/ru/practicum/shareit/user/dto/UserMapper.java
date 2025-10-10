package ru.practicum.shareit.user.dto;

import ru.practicum.shareit.user.model.User;

public class UserMapper {

    public static UserDto toDto(User u) {
        if (u == null) {
            return null;
        }
        return UserDto.builder()
                .id(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .build();
    }

    public static User fromDto(UserDto d) {
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
