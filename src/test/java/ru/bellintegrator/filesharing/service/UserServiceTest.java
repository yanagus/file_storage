package ru.bellintegrator.filesharing.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.exception.ServiceException;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.repository.UserRepository;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Тест сервиса пользователей
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MailSender mailSender;

    @InjectMocks
    private UserServiceImpl userService;

    /**
     * Тест метода, возвращающего список файлов
     */
    @Test
    public void findAllUsersTest() {
        List<User> users = Collections.singletonList(new User(1, "Maria", "maria",
                "example@example.com", null, true));
        when(userRepository.findAll()).thenReturn(users);
        List<User> users2 = userService.findAll();
        verify(userRepository).findAll();
        Assert.assertEquals(users, users2);
    }

    /**
     * Тест метода, добавляющего нового пользователя
     */
    @Test
    public void addUser() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("password");
        user.setEmail("example@example.com");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(null);
        userService.addUser(user);

        Assert.assertNotNull(user.getActivationCode());
        Assert.assertNotNull(user.getRegistrationDate());
        Assert.assertFalse(user.getIsConfirmed());

        verify(userRepository).save(user);
        verify(mailSender).send(
                ArgumentMatchers.eq(user.getEmail()),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
        );
    }

    /**
     * Тест метода, добавляющего пользователя, который уже есть в системе
     */
    @Test(expected = ServiceException.class)
    public void addExistingUser() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("password");
        user.setEmail("example@example.com");
        when(userRepository.findByUsername(user.getUsername())).thenReturn(user);
        userService.addUser(user);
    }

    /**
     * Тест метода, активирующего пользователя
     */
    @Test
    public void activateUser() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("password");
        user.setEmail("example@example.com");
        user.setActivationCode("af931428-a7aa-4957-a287-e554903fb4db");
        user.setRegistrationDate(new Date());
        user.setIsConfirmed(false);

        when(userRepository.findByActivationCode(user.getActivationCode())).thenReturn(user);
        userService.activateUser(user.getActivationCode());

        Assert.assertTrue(user.getIsConfirmed());
        Assert.assertNull(user.getActivationCode());
        verify(userRepository).save(user);
    }

    /**
     * Тест метода, активирующего пользователя с датой регистрации более 24 часов назад
     * Выбрасывает ошибку
     */
    @Test
    public void activateUserWithRegDateOver24Hours() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("password");
        user.setEmail("example@example.com");
        user.setActivationCode("af931428-a7aa-4957-a287-e554903fb4db");
        user.setRegistrationDate(new Date(1547841600000L)); //19.01.2019
        user.setIsConfirmed(false);

        when(userRepository.findByActivationCode(user.getActivationCode())).thenReturn(user);
        try {
            userService.activateUser(user.getActivationCode());
        } catch (ServiceException e) {
            Assert.assertEquals("Activation link has expired! The new one has been sent",
                                e.getMessage());
        }
        Assert.assertNotEquals(1547841600000L, user.getRegistrationDate().getTime());
        verify(mailSender).send(
                ArgumentMatchers.eq(user.getEmail()),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
        );
    }

    /**
     * Тест метода, активирующего пользователя
     * Выбрасывает ошибку при отсутствующем коде
     */
    @Test(expected = NotFoundException.class)
    public void activateUserWithEmptyCode() {
        userService.activateUser(null);
    }

    /**
     * Тест метода, активирующего пользователя
     * Выбрасывает ошибку, когда пользователь не найден в системе по данному коду
     */
    @Test(expected = NotFoundException.class)
    public void activateUserByWrongCode() {
        String code = "af931428-a7aa-4957-a287-e554903fb4db";
        when(userRepository.findByActivationCode(code)).thenReturn(null);
        userService.activateUser(code);
    }
}
