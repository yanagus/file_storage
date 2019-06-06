package ru.bellintegrator.filesharing.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.bellintegrator.filesharing.exception.AlreadyExistException;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.repository.AccessRepository;
import ru.bellintegrator.filesharing.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тест сервиса доступа
 */
@RunWith(MockitoJUnitRunner.class)
public class AccessServiceTest {

    @Mock
    private AccessRepository accessRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccessServiceImpl accessService;

    private User user = new User(1, "Maria", "maria", "example@example.com", null, true);
    private User subscriber = new User(2, "John", "john", "example@example.com", null, true);

    /**
     * Тест метода, сохраняющего запрос на чтение
     */
    @Test
    public void saveRequestToReadTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access access = new Access(user, subscriber);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.saveRequestToRead("1", subscriber);

        Assert.assertTrue(access.getReadRequest());
        Assert.assertTrue(access.getReadAccess());

        verify(accessRepository).save(access);
    }

    /**
     * Тест метода, сохраняющего запрос на чтение
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void saveRequestToReadFailTest() {
        accessService.saveRequestToRead(null, null);
    }

    /**
     * Тест метода, сохраняющего запрос на чтение
     * Выбрасывается ошибка о том, что доступ уже был предоставлен
     */
    @Test(expected = AlreadyExistException.class)
    public void saveRequestToReadExistAccessTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access access = new Access(user, subscriber);
        access.setReadAccess(true);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.saveRequestToRead("1", subscriber);
    }

    /**
     * Тест метода, сохраняющего запрос на чтение
     * Выбрасывается ошибка о том, что доступ уже был предоставлен
     */
    @Test(expected = AlreadyExistException.class)
    public void saveRequestToReadExistDownloadAccessTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access access = new Access(user, subscriber);
        access.setDownloadAccess(true);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.saveRequestToRead("1", subscriber);
    }

    /**
     * Тест метода, сохраняющего запрос на скачивание
     */
    @Test
    public void saveRequestToDownloadTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access access = new Access(user, subscriber);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.saveRequestToDownload("1", subscriber);

        Assert.assertTrue(access.getDownloadRequest());
        Assert.assertTrue(access.getDownloadAccess());

        verify(accessRepository).save(access);
    }

    /**
     * Тест метода, сохраняющего запрос на скачивание
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void saveRequestToDownloadFailTest() {
        accessService.saveRequestToDownload(null, null);
    }

    /**
     * Тест метода, сохраняющего запрос на скачивание
     * Выбрасывается ошибка о том, что доступ уже был предоставлен
     */
    @Test(expected = AlreadyExistException.class)
    public void saveRequestToDownloadExistAccessTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access access = new Access(user, subscriber);
        access.setDownloadAccess(true);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.saveRequestToDownload("1", subscriber);
    }

    /**
     * Тест метода, возвращающего список пользователей, запрашивающих доступ
     */
    @Test
    public void getRequestingAccessesTest() {
        Access access = new Access(user, subscriber);
        List<Access> accessList = Collections.singletonList(access);
        when(accessRepository.findByUser(user)).thenReturn(accessList);

        List<Access> emptyList = accessService.getRequestingAccesses(user);

        Assert.assertTrue(emptyList.isEmpty());

        access.setDownloadRequest(true);
        List<Access> filteredList = accessService.getRequestingAccesses(user);

        Assert.assertTrue(filteredList.contains(access));
    }

    /**
     * Тест метода, возвращающего список пользователей, запрашивающих доступ
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void getRequestingAccessesFailTest() {
        accessService.getRequestingAccesses(null);
    }

    /**
     * Тест метода, который дает доступ на чтение
     */
    @Test
    public void allowReadTest() {
        when(userRepository.findById(2)).thenReturn(Optional.of(subscriber));
        Access access = new Access(user, subscriber);
        access.setReadAccess(true);
        access.setReadRequest(true);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.allowRead(user, "2");

        Assert.assertFalse(access.getReadRequest());
        Assert.assertTrue(access.getReadAccess());

        verify(accessRepository).save(access);
    }

    /**
     * Тест метода, который дает доступ на чтение
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void allowReadFailTest() {
        accessService.allowRead(null, null);
    }

    /**
     * Тест метода, который дает доступ на чтение
     * Выбрасывается ошибка о том, что запрос на доступ не найден
     */
    @Test(expected = NotFoundException.class)
    public void allowReadNoAccessTest() {
        when(userRepository.findById(2)).thenReturn(Optional.of(subscriber));
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(null);

        accessService.allowRead(user, "2");
    }

    /**
     * Тест метода, который дает доступ на скачивание
     */
    @Test
    public void allowDownloadTest() {
        when(userRepository.findById(2)).thenReturn(Optional.of(subscriber));
        Access access = new Access(user, subscriber);
        access.setDownloadRequest(true);
        access.setDownloadAccess(true);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(access);

        accessService.allowDownload(user, "2");

        Assert.assertFalse(access.getDownloadRequest());
        Assert.assertTrue(access.getDownloadAccess());

        verify(accessRepository).save(access);
    }

    /**
     * Тест метода, который дает доступ на скачивание
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void allowDownloadFailTest() {
        accessService.allowDownload(null, null);
    }

    /**
     * Тест метода, который дает доступ на скачивание
     * Выбрасывается ошибка о том, что запрос на доступ не найден
     */
    @Test(expected = NotFoundException.class)
    public void allowDownloadNoAccessTest() {
        when(userRepository.findById(2)).thenReturn(Optional.of(subscriber));
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(null);

        accessService.allowDownload(user, "2");
    }

    /**
     * Тест метода, который находит доступ
     */
    @Test
    public void findAccessTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        Access expectedAccess = new Access(user, subscriber);
        when(accessRepository.findByUserAndSubscriber(user, subscriber)).thenReturn(expectedAccess);

        Access actualAccess = accessService.findAccess("1", subscriber);

        Assert.assertEquals(expectedAccess, actualAccess);

        verify(accessRepository).findByUserAndSubscriber(user, subscriber);
    }

    /**
     * Тест метода, который находит доступ
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void findAccessFailTest() {
        accessService.findAccess(null, subscriber);
    }

    /**
     * Тест метода, который находит доступ
     * Выбрасывается ошибка при непереданных пользователях
     */
    @Test(expected = NotFoundException.class)
    public void findAccessFailTest2() {
        accessService.findAccess("1", null);
    }

    /**
     * Тест метода, который находит пользователя по id
     */
    @Test
    public void findUserByIdTest() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User actualUser = accessService.findUserById("1");

        Assert.assertEquals(user, actualUser);

        verify(userRepository).findById(1);
    }

    /**
     * Тест метода, который находит пользователя по id
     */
    @Test(expected = NotFoundException.class)
    public void findUserByIdFailTest() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        User actualUser = accessService.findUserById("1");
    }
}
