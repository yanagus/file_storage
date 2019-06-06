package ru.bellintegrator.filesharing.exception;

/**
 * Ошибка о том, что такая сущность уже существует
 */
public class AlreadyExistException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке
     *
     * @param message сообщение
     */
    public AlreadyExistException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и указанием причины
     *
     * @param message сообщение
     * @param cause причина
     */
    public AlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }
}
