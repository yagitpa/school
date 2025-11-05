package ru.hogwarts.school.dto;

public record AvatarDataDto(
        byte[] data,
        String mediaType
) {
}
