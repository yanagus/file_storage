package ru.bellintegrator.filesharing.service;

import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;

import java.util.List;

/**
 * Сервис доступа к файлам
 */
public interface AccessService {

    /**
     * Сохраняет запрос на чтение файлов
     *
     * @param userId id пользователя
     * @param subscriber подписчик
     */
    void saveRequestToRead(String userId, User subscriber);

    /**
     * Сохраняет запрос на запись файлов
     *
     * @param userId id пользователя
     * @param subscriber подписчик
     */
    void saveRequestToDownload(String userId, User subscriber);

    /**
     * Получает список доступов с подписчиками, запрашивающими доступ
     *
     * @param currentUser текущий пользователь
     * @return список доступов
     */
    List<Access> getRequestingAccesses(User currentUser);

    /**
     * Разрешает просмотр файлов подписчику
     *
     * @param currentUser текущий пользователь
     * @param subscriberId id подписчика
     */
    void allowRead(User currentUser, String subscriberId);

    /**
     * Разрешает скачивание файлов подписчику
     *
     * @param currentUser текущий пользователь
     * @param subscriberId id подписчика
     */
    void allowDownload(User currentUser, String subscriberId);

    /**
     * Находит доступ
     *
     * @param userId id пользователя
     * @param subscriber подписчик
     * @return доступ
     */
    Access findAccess(String userId, User subscriber);

    /**
     * Находит пользователя по id
     *
     * @param userId id пользователя
     * @return пользователь
     */
    User findUserById(String userId);

}
