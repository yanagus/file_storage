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
        return createModelAndView(e);
    }

    /**
     * Обработчик ошибок прав доступа
     * @param e ошибка
     * @return страница с текстом ошибки
     */
    @ExceptionHandler(AccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessException(AccessException e) {
        return createModelAndView(e);
    }

    /**
     * Обработчик ошибок о существующей сущности
     * @param e ошибка
     * @return страница с текстом ошибки
     */
    @ExceptionHandler(AlreadyExistException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleAlreadyExistException(AlreadyExistException e) {
        return createModelAndView(e);
    }

    /**
     * Создать страницу с текстом ошибки
     * @param throwable перехватываемое исключение
     * @return страница с текстом ошибки
     */
    private ModelAndView createModelAndView(Throwable throwable) {
        ModelAndView mav = new ModelAndView();
        mav.addObject("error", throwable.getMessage());
        mav.setViewName("error");
        return mav;
    }
}
