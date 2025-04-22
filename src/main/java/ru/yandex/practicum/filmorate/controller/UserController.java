package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    @PostMapping
    public User createUser(@RequestBody User user) {
        validateUserForCreate(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            String message = String.format("Пользователь с id=%s не найден.", user.getId());
            log.warn(message);
            throw new NoSuchElementException(message);
        }

        User existingUser = users.get(user.getId());

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


    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
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