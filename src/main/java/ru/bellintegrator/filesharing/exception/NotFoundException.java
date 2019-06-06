package ru.bellintegrator.filesharing.exception;

/**
 * Ошибка о том, что такая сущность не найдена
 */
public class NotFoundException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке
     *
     * @param message сообщение
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и указанием причины
     *
     * @param message сообщение
     * @param cause причина
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
