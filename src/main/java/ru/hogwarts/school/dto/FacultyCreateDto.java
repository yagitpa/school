package ru.hogwarts.school.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FacultyCreateDto(
        @NotBlank(message = "Faculty Name is mandatory")
        @Size(min = 3, max = 100, message = "Faculty Name must be between 3 and 100 characters")
        String name,

        @NotBlank(message = "Color is mandatory")
        @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hex color")
        String color
) {
    public FacultyCreateDto {
        if (name != null) {
            name = name.trim();
        }
        if (color != null) {
            color = color.trim();
        }
    }
}
