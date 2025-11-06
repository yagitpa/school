package ru.hogwarts.school.exception;

public class StudentNotFoundException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Student with ID %d not found";
    private static final String ERROR_CODE = "STUDENT_NOT_FOUND";

    public StudentNotFoundException(Long studentId) {
        super(String.format(MESSAGE_TEMPLATE, studentId), ERROR_CODE);
    }

    public StudentNotFoundException(Long studentId, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, studentId), ERROR_CODE, cause);
    }
}
