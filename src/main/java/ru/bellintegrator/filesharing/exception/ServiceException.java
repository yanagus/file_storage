package ru.bellintegrator.filesharing.exception;

/**
 * Ошибка сервиса
 */
public class ServiceException extends RuntimeException {

    /**
     * Конструктор с сообщением об ошибке
     *
     * @param message сообщение
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * Конструктор с сообщением об ошибке и передачей причины
     *
     * @param message сообщение
     * @param cause причина
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
