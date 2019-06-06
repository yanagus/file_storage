package ru.bellintegrator.filesharing.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.bellintegrator.filesharing.model.User;

import java.util.List;

/**
 * Сервис пользователей
 */
public interface UserService extends UserDetailsService {

    /**
     * Находит всех пользователей
     *
     * @return список пользователей
     */
    List<User> findAll();

    /**
     * Добавляет нового пользователя
     *
     * @param user пользователь
     */
    void addUser(User user);

    /**
     * Активирует пользователя
     *
     * @param code код активации
     */
    void activateUser(String code);

}
