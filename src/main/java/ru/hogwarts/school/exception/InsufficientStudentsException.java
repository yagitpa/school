package ru.hogwarts.school.exception;

public class InsufficientStudentsException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Insufficient students for operation. Required: %d, found: %d";
    public static final String ERROR_CODE = "INSUFFICIENT_STUDENTS";

    public InsufficientStudentsException(int required, int found) {
        super(String.format(MESSAGE_TEMPLATE, required, found), ERROR_CODE);
    }

    public InsufficientStudentsException(String message) {
        super(message, ERROR_CODE);
    }
}
