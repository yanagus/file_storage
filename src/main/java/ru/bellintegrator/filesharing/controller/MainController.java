package ru.bellintegrator.filesharing.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Главный контроллер
 */
@Controller
public class MainController {

    /**
     * Отображает страницу приветствия
     *
     * @return страница приветствия
     */
    @GetMapping("/")
    public String showGreeting() {
        return "greeting";
    }
}
