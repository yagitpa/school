package ru.hogwarts.school.exception;

public class FacultyNotFoundException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Faculty with ID %d not found";
    private static final String STUDENT_NO_FACULTY_MESSAGE = "Student with ID %d has no Faculty";
    private static final String ERROR_CODE = "FACULTY_NOT_FOUND";

    public FacultyNotFoundException(Long facultyId) {
        super(String.format(MESSAGE_TEMPLATE, facultyId), ERROR_CODE);
    }

    public FacultyNotFoundException(Long facultyId, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, facultyId), ERROR_CODE, cause);
    }

    public FacultyNotFoundException(String message) {
        super(message, ERROR_CODE);
    }

    public static FacultyNotFoundException forStudentWithoutFaculty(Long studentId) {
        return new FacultyNotFoundException(String.format(STUDENT_NO_FACULTY_MESSAGE, studentId));
    }
}
