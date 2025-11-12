package ru.hogwarts.school.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.hogwarts.school.dto.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HogwartsException.class)
    public ResponseEntity<ErrorResponse> handleHogwartsException(HogwartsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                ex.getMessage(),
                List.of(ex.getErrorCode())
        );

        HttpStatus status = determineHttpStatus(ex);
        return ResponseEntity.status(status).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse("Validation Failed", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body("Invalid JSON format: " + ex.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<String> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                             .body("Missing required parameter: " + ex.getParameterName());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "Internal Server Error",
                List.of("An unexpected error occurred")
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private HttpStatus determineHttpStatus(HogwartsException ex) {
        return switch (ex.getErrorCode()) {
            case "STUDENT_NOT_FOUND", "FACULTY_NOT_FOUND", "AVATAR_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_FILE", "INSUFFICIENT_STUDENTS" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}