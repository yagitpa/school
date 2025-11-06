package ru.hogwarts.school.exception;

public class AvatarNotFoundException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Avatar not found for Student with ID %d";
    private static final String ERROR_CODE = "AVATAR_NOT_FOUND";

    public AvatarNotFoundException(Long studentId) {
        super(String.format(MESSAGE_TEMPLATE, studentId), ERROR_CODE);
    }
}