package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        validateUserForCreate(user);
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        User existingUser = userStorage.getUserById(user.getId());
        if (existingUser == null) {
            String message = String.format("Пользователь с id=%s не найден.", user.getId());
            log.warn(message);
            throw new NoSuchElementException(message);
        }

        // Обновим только те поля, которые были переданы
        if (user.getEmail() != null) {
            validateEmail(user.getEmail());
            existingUser.setEmail(user.getEmail());
        }

        if (user.getLogin() != null) {
            validateLogin(user.getLogin());
            existingUser.setLogin(user.getLogin());
        }

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        if (user.getBirthday() != null) {
            validateBirthday(user.getBirthday());
            existingUser.setBirthday(user.getBirthday());
        }

        // Если имя остаётся пустым, используем логин
        if (existingUser.getName() == null || existingUser.getName().isBlank()) {
            existingUser.setName(existingUser.getLogin());
            log.info("Отображаемое имя не задано. Использован логин вместо имени: {}", existingUser.getLogin());
        }

        log.info("Пользователь обновлён: {}", existingUser);
        return existingUser;
    }

    public User getUserById(Long userId) {
        User user = userStorage.getUserById(userId);
        if (user == null) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }
        return user;
    }

    public Collection<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public void addFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.addFriend(friendId);
        friend.addFriend(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        User user = getUserById(userId);
        User friend = getUserById(friendId);

        user.removeFriend(friendId);
        friend.removeFriend(userId);
    }

    public List<User> getFriends(Long userId) {
        User user = getUserById(userId);
        return user.getFriends().stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        User user1 = getUserById(userId);
        User user2 = getUserById(otherUserId);

        Set<Long> commonFriendIds = new HashSet<>(user1.getFriends());
        commonFriendIds.retainAll(user2.getFriends());

        return commonFriendIds.stream()
                .map(this::getUserById)
                .collect(Collectors.toList());
    }

    private void validateUserForCreate(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            String error = "Недопустимый email";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            String error = "Логин не может быть пустым или содержать пробелы";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            String error = "Дата рождения не может быть в будущем";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Отображаемое имя не задано. Использован логин вместо имени: {}", user.getLogin());
        }
    }

    private void validateEmail(String email) {
        if (email.isBlank() || !email.contains("@")) {
            String error = "Недопустимый email";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
    }

    private void validateLogin(String login) {
        if (login.isBlank() || login.contains(" ")) {
            String error = "Логин не может быть пустым или содержать пробелы";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday.isAfter(LocalDate.now())) {
            String error = "Дата рождения не может быть в будущем";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
    }

}