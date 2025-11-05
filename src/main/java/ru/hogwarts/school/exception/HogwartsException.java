package ru.hogwarts.school.exception;

public abstract class HogwartsException extends RuntimeException {
    private final String errorCode;

    public HogwartsException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public HogwartsException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
