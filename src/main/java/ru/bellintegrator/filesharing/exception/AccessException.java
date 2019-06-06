package ru.bellintegrator.filesharing.exception;

/**
 * Ошибка о том, что у пользователя нет прав доступа
 */
public class AccessException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке
     *
     * @param message сообщение
     */
    public AccessException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и указанием причины
     *
     * @param message сообщение
     * @param cause причина
     */
    public AccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
