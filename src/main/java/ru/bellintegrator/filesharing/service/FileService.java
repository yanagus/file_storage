package ru.bellintegrator.filesharing.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.model.UserFile;

import java.util.List;

/**
 * Сервис файлов
 */
public interface FileService {

    /**
     * Находит все файлы в системе
     *
     * @return список файлов
     */
    List<UserFile> findAllFiles();

    /**
     * Добавляет файл в систему
     *
     * @param currentUser текущий пользователь
     * @param file файл
     */
    void uploadFile(User currentUser, MultipartFile file);

    /**
     * Загружает файл из системы
     *
     * @param currentUser текущий пользователь
     * @param fileId id файла
     * @return Resource
     */
    Resource downloadFile(User currentUser, String fileId);

    /**
     * Удаляет файл
     *
     * @param currentUser текущий пользователь
     * @param fileId id файла
     */
    void deleteFile(User currentUser, String fileId);

}
