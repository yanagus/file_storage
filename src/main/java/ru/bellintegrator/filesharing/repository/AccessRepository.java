package ru.bellintegrator.filesharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.AccessId;
import ru.bellintegrator.filesharing.model.User;

import java.util.List;

/**
 * Репозиторий для работы доступом к файлам пользователей
 */
public interface AccessRepository extends JpaRepository<Access, AccessId> {

    /**
     * Находит список доступов посписчиков по пользователю
     *
     * @param user пользователь
     * @return список доступов
     */
    List<Access> findByUser(User user);

    /**
     * Находит доступ подписчика по пользователю и подписчику
     *
     * @param user пользователь
     * @param subscriber его подписчик
     * @return доступ
     */
    Access findByUserAndSubscriber(User user, User subscriber);

}
