package ru.bellintegrator.filesharing.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Обработчик ошибок
 */
@ControllerAdvice
public class ApplicationExceptionHandler {

    /**
     * Обработчик ошибок ненайденных сущностей
     * @param e ошибка
     * @return страница с текстом ошибки
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleWeatherException(NotFoundException e) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", e.getMessage());
        mav.setViewName("error");
        return mav;
    }
}
