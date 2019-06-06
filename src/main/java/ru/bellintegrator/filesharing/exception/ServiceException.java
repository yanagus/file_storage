package ru.bellintegrator.filesharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка сервиса
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
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
