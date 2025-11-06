package ru.hogwarts.school.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        String message,
        LocalDateTime timeStamp,
        List<String> details
) {
    public ErrorResponse(String message, List<String> details) {
        this(message, LocalDateTime.now(), details);
    }

    public ErrorResponse(String message) {
        this(message, LocalDateTime.now(), List.of());
    }
}
