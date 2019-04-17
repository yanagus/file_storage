package ru.bellintegrator.filesharing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.bellintegrator.filesharing.model.UserFile;

/**
 * Репозиторий для работы с файлами
 */
public interface UserFileRepository extends JpaRepository<UserFile, Integer> {

}
