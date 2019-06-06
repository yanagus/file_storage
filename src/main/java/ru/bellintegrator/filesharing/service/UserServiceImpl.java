package ru.bellintegrator.filesharing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.exception.ServiceException;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.repository.UserRepository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * {@inheritDoc}
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MailSender mailSender;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, MailSender mailSender) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
    }

    /**
     * Находит пользователя по имени
     *
     * @param username имя пользователя
     * @return UserDetails
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void addUser(User user) {
        User userFromDb = userRepository.findByUsername(user.getUsername());

        if (userFromDb != null) {
            throw new ServiceException("User exists!");
        }

        setActivationCodeAndRegDate(user);
        sendActivationCode(user);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void activateUser(String code) {
        if (code == null) {
            throw new NotFoundException("Activation code has not found!");
        }

        User user = userRepository.findByActivationCode(code);

        if (user == null) {
            throw new NotFoundException("Activation code has not found!");
        }

        if (checkRegDate(user.getRegistrationDate())) {
            setActivationCodeAndRegDate(user);
            sendActivationCode(user);
            throw new ServiceException("Activation link has expired! The new one has been sent");
        }

        user.setActivationCode(null);
        user.setIsConfirmed(true);
        user.setPassword2(user.getPassword());
        userRepository.save(user);
    }

    /**
     * Устанавливает код активации и дату регистрации
     *
     * @param user пользователь
     */
    private void setActivationCodeAndRegDate(User user) {
        user.setActivationCode(UUID.randomUUID().toString());
        user.setRegistrationDate(new Date());
        userRepository.save(user);
    }

    /**
     * Посылает на e-mail пользователю ссылку для подтверждения регистрации
     *
     * @param user пользователь
     */
    private void sendActivationCode(User user) {
        String message = String.format(
                "Hello, %s! \n" +
                        "Welcome to File Sharing! Please, visit next link to confirm your e-mail: http://localhost:8080/activate/%s",
                user.getUsername(),
                user.getActivationCode()
        );
        mailSender.send(user.getEmail(), "Activation code", message);
    }

    /**
     * Проверяет зарегистрировался ли пользователь более суток назад
     *
     * 86400000 - это 24 часа в милисекундах
     *
     * @param regDate дата регистрации пользователя
     * @return true - если с момента регистрации прошло более 24 часов
     */
    private boolean checkRegDate(Date regDate) {
        return (System.currentTimeMillis() - regDate.getTime()) > 86400000;
    }
}
