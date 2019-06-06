package ru.bellintegrator.filesharing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.bellintegrator.filesharing.exception.AccessException;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.model.UserFile;
import ru.bellintegrator.filesharing.repository.AccessRepository;
import ru.bellintegrator.filesharing.repository.UserFileRepository;

import javax.persistence.EntityNotFoundException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * {@inheritDoc}
 */
@Service
public class FileServiceImpl implements FileService {

    @Value("${upload.path}")
    private String uploadPath;

    private final UserFileRepository fileRepository;
    private final AccessRepository accessRepository;

    @Autowired
    public FileServiceImpl(UserFileRepository fileRepository, AccessRepository accessRepository) {
        this.fileRepository = fileRepository;
        this.accessRepository = accessRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<UserFile> findAllFiles() {
        return fileRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void uploadFile(User currentUser, MultipartFile file) {
        if (file == null || StringUtils.isEmpty(file.getOriginalFilename())) {
            throw new NotFoundException("Select file!");
        }
        File uploadDir = new File(uploadPath);

        if (!uploadDir.exists()) {
            uploadDir.mkdir();
        }

        String resultFilename = createUniqueFileName(file.getOriginalFilename());

        try {
            file.transferTo(new File(uploadPath + "/" + resultFilename));
        } catch (IOException | IllegalStateException e) {
            throw new NotFoundException("The file or path was not found!", e);
        }

        UserFile userFile = new UserFile();
        userFile.setOriginalName(file.getOriginalFilename());
        userFile.setFileName(resultFilename);
        userFile.setUser(currentUser);
        userFile.setDownloadCount(0);
        fileRepository.save(userFile);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public Resource downloadFile(User currentUser, String fileId) {
        Integer id = transformStringIdToInteger(fileId);
        UserFile userFile = fileRepository.getOne(id);
        try {
            Access access = accessRepository.findByUserAndSubscriber(userFile.getUser(), currentUser);
            if (isFileOwner(currentUser, userFile.getUser()) || (access != null
                    && !access.getDownloadRequest() && access.getDownloadAccess())) {
                Path rootLocation = Paths.get(uploadPath);
                Path file = rootLocation.resolve(userFile.getFileName());
                Resource resource = new UrlResource(file.toUri());
                if (!resource.exists() || !resource.isReadable()) {
                    throw new NotFoundException("Could not read file: " + userFile.getFileName());
                }
                userFile.setDownloadCount(userFile.getDownloadCount() + 1);
                fileRepository.save(userFile);
                return resource;
            }
            else throw new AccessException("You need permission to perform this action!");
        }
        catch (MalformedURLException e) {
            throw new NotFoundException("Could not read file: " + userFile.getFileName(), e);
        }
        catch (EntityNotFoundException e) {
            throw new NotFoundException("There is no file with id " + id, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void deleteFile(User currentUser, String fileId) {
        Integer id = transformStringIdToInteger(fileId);
        try {
            UserFile userFile = fileRepository.getOne(id);
            if (!isFileOwner(currentUser, userFile.getUser())) {
                throw new AccessException("You can not delete not your file!");
            }
            File fileFromDisk = new File(uploadPath + "/" + userFile.getFileName());
            if (!fileFromDisk.delete()) {
                throw new NotFoundException("The file was not deleted!");
            }
            fileRepository.delete(userFile);
        } catch (EntityNotFoundException e) {
            throw new NotFoundException("There is no file with id " + id, e);
        }
    }

    /**
     * Создает уникальное имя файла
     *
     * @param originalFileName оригинальное имя файла
     * @return уникальное имя файла
     */
    private String createUniqueFileName(String originalFileName){
        String uuidFile = UUID.randomUUID().toString();
        return uuidFile + "." + originalFileName;
    }

    /**
     * Меняет тип id со String на Integer
     * @param fileId id файла
     * @return Integer
     */
    private Integer transformStringIdToInteger(String fileId) {
        if (fileId == null || !fileId.matches("[\\d]+")) {
            throw new NotFoundException("The file id must not be null or character!");
        }
        return Integer.valueOf(fileId);
    }

    /**
     * Определяет является ли текущий пользователь владельцем файлов
     *
     * @param currentUser текущий пользователь
     * @param fileOwner владелец файлов
     * @return true, если пользователь один и тот же
     */
    private boolean isFileOwner(User currentUser, User fileOwner) {
        return currentUser.getId().equals(fileOwner.getId());
    }
}
