package ru.bellintegrator.filesharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bellintegrator.filesharing.model.User;

/**
 * Репозиторий для работы с пользователями
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Находит пользователя по имени
     *
     * @param username имя
     * @return пользователь
     */
    User findByUsername(String username);

    /**
     * Находит пользователя по коду активации
     *
     * @param activationCode код активации
     * @return пользователь
     */
    User findByActivationCode(String activationCode);
}
