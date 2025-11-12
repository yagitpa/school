package ru.hogwarts.school.exception;

public class ThreadExecutionException extends HogwartsException {
    private static final String MESSAGE_TEMPLATE = "Thread execution failed during %s operation";
    private static final String ERROR_CODE = "THREAD_EXECUTION_ERROR";

    public ThreadExecutionException(String operation, Throwable cause) {
        super(String.format(MESSAGE_TEMPLATE, operation), ERROR_CODE, cause);
    }

    public ThreadExecutionException(String message) {
        super(message, ERROR_CODE);
    }
}
