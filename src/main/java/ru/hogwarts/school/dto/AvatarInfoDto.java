package ru.hogwarts.school.dto;

public record AvatarInfoDto(
        Long id,
        String filePath,
        long fileSize,
        String mediaType,
        Long studentId
) {
}
