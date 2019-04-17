package ru.bellintegrator.filesharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Ошибка о том, что такая сущность не найдена
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
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
