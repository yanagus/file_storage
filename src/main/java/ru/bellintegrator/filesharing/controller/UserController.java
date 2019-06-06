package ru.bellintegrator.filesharing.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.bellintegrator.filesharing.exception.ServiceException;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.service.UserService;

import javax.validation.Valid;
import java.util.Map;

/**
 * Контроллер пользователей
 */
@Controller
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Отображает список всех пользователей
     *
     * @param model модель
     * @return страница со списком пользователей
     */
    @GetMapping("/users")
    public String showUserList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "userList";
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model)
    {
        if (error != null) {
            model.addAttribute("message", "Invalid username and password!");
        }

        if (logout != null) {
            model.addAttribute("info", "You have been logged out successfully.");
        }
        return "login";
    }

    /**
     * Отображает форму регистрации
     *
     * @return страница с формой рестрации
     */
    @GetMapping("/registration")
    public String showRegistrationForm() {
        return "registration";
    }

    /**
     * Добавляет нового пользователя в систему
     *
     * @param user пользователь
     * @param bindingResult интерфейс для регистрации ошибок валидации
     * @param model модель
     * @return страница с формой рестрации
     */
    @PostMapping("/registration")
    public String addUser(@Valid User user, BindingResult bindingResult, Model model) {
        if (user.getPassword() != null && !user.getPassword().equals(user.getPassword2())) {
            bindingResult.addError(new FieldError("login", "password", "Passwords are different!"));
        }
        if(bindingResult.hasErrors()) {
            Map<String, String> errors = ControllerUtils.getErrors(bindingResult);
            model.mergeAttributes(errors);
            return "registration";
        }

        try {
            userService.addUser(user);
            model.addAttribute("info", "Activate your account through the link from your e-mail");
            return "login";
        } catch (ServiceException e) {
            model.addAttribute("message", e.getMessage());
            return "registration";
        }
    }

    /**
     * Активирует учётную запись
     *
     * @param model модель
     * @param code регистрационный код
     * @return страница с формой авторизации
     */
    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        try {
            userService.activateUser(code);
            model.addAttribute("info", "User successfully activated");
        } catch (ServiceException e) {
            model.addAttribute("message", e.getMessage());
        }
        return "login";
    }
}
