package ru.bellintegrator.filesharing.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import ru.bellintegrator.filesharing.UserFileMatcher;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

/**
 * Тест сервиса файлов
 */
@RunWith(MockitoJUnitRunner.class)
public class FileServiceTest {

    @Mock
    private UserFileRepository fileRepository;

    @Mock
    private AccessRepository accessRepository;

    @InjectMocks
    private FileServiceImpl fileService;

    private User fileOwner = new User(1, "Maria", "maria", "example@example.com", null, true);
    private User notOwner = new User(2, "John", "john", "example@example.com", null, true);

    @Before
    public void init() {
        ReflectionTestUtils.setField(fileService, "uploadPath", "src/test/resources/uploads");
    }

    /**
     * Тест метода, возвращающего список файлов
     */
    @Test
    public void findAllFilesTest() {
        List<UserFile> files = Collections.singletonList(new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0));
        Mockito.doReturn(files).when(fileRepository).findAll();
        List<UserFile> files2 = fileService.findAllFiles();
        verify(fileRepository).findAll();
        Assert.assertEquals(files, files2);
    }

    /**
     * Тест метода сохранения файла
     */
    @Test
    public void uploadFileTest() {
        MultipartFile multipartFile = new MockMultipartFile("test2", "test2.txt", MediaType.TEXT_PLAIN_VALUE,
                "test".getBytes());
        fileService.uploadFile(fileOwner, multipartFile);
        verify(fileRepository).save(argThat(new UserFileMatcher(new UserFile(null,
                "5086663c-c4d0-47ca-907d-4be2020355fc.test2.txt", "test2.txt", 0))));
        File dir = new File("src/test/resources/uploads");
        File[] matchingFiles = dir.listFiles((uploadPath, name) -> name.endsWith("test2.txt"));
        Assert.assertNotNull(matchingFiles);
        File file = Arrays.stream(matchingFiles)
                .filter(f -> f.getName().endsWith("test2.txt"))
                .findFirst()
                .orElse(null);
        Assert.assertTrue(file.exists());
        file.delete();
    }

    /**
     * Тест метода загрузки файла владельцем
     *
     * @throws MalformedURLException
     */
    @Test
    public void downloadFileByOwnerTest() throws MalformedURLException {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);

        Resource testResource = fileService.downloadFile(fileOwner, "5");
        Assert.assertTrue(userFile.getDownloadCount() == 1);

        verify(accessRepository).findByUserAndSubscriber(fileOwner, fileOwner);
        verify(fileRepository).save(userFile);

        Path rootLocation = Paths.get("src/test/resources/uploads");
        Path file = rootLocation.resolve(userFile.getFileName());
        Resource resource = new UrlResource(file.toUri());
        Assert.assertEquals(resource, testResource);
    }

    /**
     * Тест метода загрузки файла не владельцем, но имеющим доступ на скачивание
     */
    @Test
    public void downloadFileByNotOwnerTest() throws MalformedURLException {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);

        Access access = new Access(fileOwner, notOwner);
        access.setDownloadAccess(true);
        Mockito.doReturn(access).when(accessRepository).findByUserAndSubscriber(fileOwner, notOwner);

        Resource testResource = fileService.downloadFile(notOwner, "5");
        Assert.assertTrue(userFile.getDownloadCount() == 1);

        verify(accessRepository).findByUserAndSubscriber(fileOwner, notOwner);
        verify(fileRepository).save(userFile);

        Path rootLocation = Paths.get("src/test/resources/uploads");
        Path file = rootLocation.resolve(userFile.getFileName());
        Resource resource = new UrlResource(file.toUri());
        Assert.assertEquals(resource, testResource);
    }

    /**
     * Тест метода загрузки файла не владельцем и не имеющим доступа
     */
    @Test(expected = AccessException.class)
    public void downloadFileByNotOwnerFailTest() {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);

        Access access = null;
        Mockito.doReturn(access).when(accessRepository).findByUserAndSubscriber(fileOwner, notOwner);

        fileService.downloadFile(notOwner, "5");

        verify(accessRepository).findByUserAndSubscriber(fileOwner, fileOwner);
        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода загрузки файла не владельцем, имещим доступ только на чтение
     */
    @Test(expected = AccessException.class)
    public void downloadFileByNotOwnerFailTest2() {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);

        Access access = new Access(fileOwner, notOwner);
        access.setReadAccess(true);
        Mockito.doReturn(access).when(accessRepository).findByUserAndSubscriber(fileOwner, notOwner);

        fileService.downloadFile(notOwner, "5");

        verify(accessRepository).findByUserAndSubscriber(fileOwner, fileOwner);
        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода загрузки файла не владельцем, запросившим доступ
     */
    @Test(expected = AccessException.class)
    public void downloadFileByNotOwnerFailTest3() {
        UserFile userFile = new UserFile(5,
                "47fb4801-10e9-49a7-a3c4-ffb34db0f1cc.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);

        Access access = new Access(fileOwner, notOwner);
        access.setDownloadRequest(true);
        Mockito.doReturn(access).when(accessRepository).findByUserAndSubscriber(fileOwner, notOwner);

        fileService.downloadFile(notOwner, "5");

        verify(accessRepository).findByUserAndSubscriber(fileOwner, fileOwner);
        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода загрузки файла, если такого файла нет в базе данных
     */
    @Test(expected = NotFoundException.class)
    public void downloadFileFailTest() {
        Mockito.doThrow(NotFoundException.class).when(fileRepository).getOne(5);
        fileService.downloadFile(fileOwner,"5");
        verify(fileRepository, times(0)).save(ArgumentMatchers.any(UserFile.class));
    }

    /**
     * Тест метода загрузки файла, если такого файла нет на диске
     */
    @Test(expected = NotFoundException.class)
    public void downloadFileFailTest2() {
        UserFile userFile = new UserFile(5,
                "a1b55e20-68a3-4d28-a65f-6b3df5a7378f.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);
        fileService.downloadFile(fileOwner,"5");
        verify(fileRepository, times(0)).save(ArgumentMatchers.any(UserFile.class));
        Assert.assertTrue(userFile.getDownloadCount() == 0);
    }

    /**
     * Тест метода удаления файла владельцем
     *
     * @throws IOException
     */
    @Test
    public void deleteFileTest() throws IOException {
        File file = new File("src/test/resources/uploads/5086663c-c4d0-47ca-907d-4be2020355fc.test2.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        Assert.assertTrue(file.exists());
        UserFile userFile = new UserFile(5,
                "5086663c-c4d0-47ca-907d-4be2020355fc.test2.txt", "test2.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);
        fileService.deleteFile(fileOwner,"5");

        verify(fileRepository).getOne(5);
        verify(fileRepository).delete(userFile);
        Assert.assertFalse(file.exists());
    }

    /**
     * Тест метода удаления файла не владельцем
     */
    @Test(expected = AccessException.class)
    public void deleteFileByNotOwnerTest() {
        UserFile userFile = new UserFile(5,
                "5086663c-c4d0-47ca-907d-4be2020355fc.test2.txt", "test2.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);
        fileService.deleteFile(notOwner,"5");

        verify(fileRepository).getOne(5);
        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода удаления файла по Id = null
     */
    @Test(expected = NotFoundException.class)
    public void deleteByNullFileIdFailTest() {
        fileService.deleteFile(fileOwner, null);

        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода удаления файла, если id - не цифра
     */
    @Test(expected = NotFoundException.class)
    public void deleteByCharacterFileIdFailTest() {
        fileService.deleteFile(fileOwner, "b");

        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода удаления файла, если файл не найден в базе данных
     */
    @Test(expected = NotFoundException.class)
    public void deleteNotExistFileFailTest() {
        Mockito.doThrow(EntityNotFoundException.class).when(fileRepository).getOne(5);
        fileService.deleteFile(fileOwner, "5");

        verify(fileRepository).getOne(ArgumentMatchers.any(Integer.class));
        verifyNoMoreInteractions(fileRepository);
    }

    /**
     * Тест метода удаления файла, если файл найден в базе данных, но не найден на диске
     */
    @Test(expected = NotFoundException.class)
    public void deleteNotExistFileFailTest2() {
        UserFile userFile = new UserFile(5,
                "2b2bddef-15f1-4469-bbcb-9d72505df459.test.txt", "test.txt", 0);
        userFile.setUser(fileOwner);
        Mockito.doReturn(userFile).when(fileRepository).getOne(5);
        File file = new File("src/test/resources/uploads/2b2bddef-15f1-4469-bbcb-9d72505df459.test.txt");

        Assert.assertFalse(file.exists());

        fileService.deleteFile(fileOwner, "5");

        verify(fileRepository).getOne(ArgumentMatchers.any(Integer.class));
        verifyNoMoreInteractions(fileRepository);
    }

}
