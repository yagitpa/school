package ru.hogwarts.school.exception;

public class InvalidFileException extends HogwartsException {
    private static final String EMPTY_FILE_MESSAGE = "Uploaded file is empty or invalid";
    private static final String MISSING_EXTENSION_MESSAGE = "File extension is missing or invalid";
    private static final String ERROR_CODE = "INVALID_FILE";

    public InvalidFileException(String message) {
        super(message, ERROR_CODE);
    }

    public static InvalidFileException emptyFile() {
        return new InvalidFileException(EMPTY_FILE_MESSAGE);
    }

    public static InvalidFileException missingExtension() {
        return new InvalidFileException(MISSING_EXTENSION_MESSAGE);
    }
}