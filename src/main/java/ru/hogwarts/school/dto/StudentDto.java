package ru.hogwarts.school.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentDto(
        Long id,

        @NotBlank(message = "Student name is mandatory")
        @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
        String name,

        @Min(value = 15, message = "Student must be at least 15 years old")
        @Max(value = 100, message = "Student age must be reasonable")
        int age,

        Long facultyId
) {

    public StudentDto {
        if (name != null) {
            name = name.trim();
        }
    }
}