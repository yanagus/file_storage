package ru.bellintegrator.filesharing.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
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
     * @param file файл
     */
    void uploadFile(MultipartFile file);

    /**
     * Загружает файл из системы
     *
     * @param fileId id файла
     * @return Resource
     */
    Resource downloadFile(String fileId);

    /**
     * Удаляет файл
     *
     * @param fileId id файла
     */
    void deleteFile(String fileId);

}
