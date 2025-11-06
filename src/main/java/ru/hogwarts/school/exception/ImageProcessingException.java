package ru.hogwarts.school.exception;

public class ImageProcessingException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Failed to process image during %s";
    private static final String IMAGE_READING_MESSAGE = "Could not read image file";
    private static final String ERROR_CODE = "IMAGE_PROCESSING_ERROR";

    public ImageProcessingException(String operation) {
        super(String.format(MESSAGE_TEMPLATE, operation), ERROR_CODE);
    }

    public ImageProcessingException(String operation, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, operation), ERROR_CODE, cause);
    }

    public static ImageProcessingException forImageReading() {
        return new ImageProcessingException(IMAGE_READING_MESSAGE);
    }
}