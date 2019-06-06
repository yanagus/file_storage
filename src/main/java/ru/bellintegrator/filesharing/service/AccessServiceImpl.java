package ru.bellintegrator.filesharing.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bellintegrator.filesharing.exception.AlreadyExistException;
import ru.bellintegrator.filesharing.exception.NotFoundException;
import ru.bellintegrator.filesharing.model.Access;
import ru.bellintegrator.filesharing.model.User;
import ru.bellintegrator.filesharing.repository.AccessRepository;
import ru.bellintegrator.filesharing.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@inheritDoc}
 */
@Service
public class AccessServiceImpl implements AccessService {

    private final AccessRepository accessRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccessServiceImpl(AccessRepository accessRepository, UserRepository userRepository) {
        this.accessRepository = accessRepository;
        this.userRepository = userRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void saveRequestToRead(String userId, User subscriber) {
        checkUser(subscriber);
        User user = findUserById(userId);
        Access access = accessRepository.findByUserAndSubscriber(user, subscriber);
        if (access == null) {
            access = new Access(userRepository.getOne(user.getId()), userRepository.getOne(subscriber.getId()));
        }
        if (access.getDownloadAccess() && !access.getDownloadRequest()) {
            throw new AlreadyExistException("You already have the permission to access!");
        }
        if (access.getReadAccess() && !access.getReadRequest()) {
            throw new AlreadyExistException("You already have the permission to read!");
        }
        access.setReadAccess(true);
        access.setReadRequest(true);
        accessRepository.save(access);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void saveRequestToDownload(String userId, User subscriber) {
        checkUser(subscriber);
        User user = findUserById(userId);
        Access access = accessRepository.findByUserAndSubscriber(user, subscriber);
        if (access == null) {
            access = new Access(userRepository.getOne(user.getId()), userRepository.getOne(subscriber.getId()));
        }
        if (access.getDownloadAccess() && !access.getDownloadRequest()) {
            throw new AlreadyExistException("You already have the permission to download!");
        }
        access.setDownloadAccess(true);
        access.setDownloadRequest(true);
        accessRepository.save(access);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public List<Access> getRequestingAccesses(User currentUser) {
        if (currentUser == null) {
            throw new NotFoundException("No user");
        }
        return accessRepository.findByUser(currentUser)
                .stream()
                .filter(access -> access.getReadRequest() || access.getDownloadRequest())
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void allowRead(User currentUser, String subscriberId) {
        checkUser(currentUser);
        User subscriber = findUserById(subscriberId);
        Access access = accessRepository.findByUserAndSubscriber(currentUser, subscriber);
        if (access == null) {
            throw new NotFoundException("There is no requesting access");
        }
        access.setReadRequest(false);
        accessRepository.save(access);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional
    @Override
    public void allowDownload(User currentUser, String subscriberId) {
        checkUser(currentUser);
        User subscriber = findUserById(subscriberId);
        Access access = accessRepository.findByUserAndSubscriber(currentUser, subscriber);
        if (access == null) {
            throw new NotFoundException("There is no requesting access");
        }
        access.setDownloadRequest(false);
        accessRepository.save(access);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public Access findAccess(String userId, User subscriber) {
        checkUser(subscriber);
        User user = findUserById(userId);
        return accessRepository.findByUserAndSubscriber(user, subscriber);
    }

    /**
     * {@inheritDoc}
     */
    @Transactional(readOnly = true)
    @Override
    public User findUserById(String userId) {
        Integer id = transformStringIdToInteger(userId);
        Optional<User> optional = userRepository.findById(id);
        if (!optional.isPresent()) {
            throw new NotFoundException("No user or subscriber");
        }
        return optional.get();
    }

    /**
     * Проверяет пользователя и подписчика на пустоту
     *
     * @param user пользователь
     */
    private void checkUser(User user) {
        if (user == null) {
            throw new NotFoundException("No user or subscriber");
        }
    }

    /**
     * Меняет тип id со String на Integer
     *
     * @param userId id пользователя
     * @return Integer
     */
    private Integer transformStringIdToInteger(String userId) {
        if (userId == null || !userId.matches("[\\d]+")) {
            throw new NotFoundException("The user id must not be null or character!");
        }
        return Integer.valueOf(userId);
    }
}
