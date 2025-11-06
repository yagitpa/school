package ru.hogwarts.school.exception;

public class FileProcessingException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Failed to process file during %s operation";
    private static final String ERROR_CODE = "FILE_PROCESSING_ERROR";

    public FileProcessingException(String operation, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, operation), ERROR_CODE, cause);
    }

    public FileProcessingException(String message) {
        super(message, ERROR_CODE);
    }
}