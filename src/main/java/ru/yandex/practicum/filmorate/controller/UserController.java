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
        validateUser(user);
        user.setId(nextId++);
        users.put(user.getId(), user);
        log.info("Создан пользователь: {}", user);
        return user;
    }

    @PutMapping
    public User updateUser(@RequestBody User user) { validateUser(user);
        if (!users.containsKey(user.getId())) {
            String message = "Пользователь с id=" + user.getId() + " не найден.";
            log.warn(message);
            throw new NoSuchElementException(message);
        }
        users.put(user.getId(), user);
        log.info("Пользователь обновлён: {}", user);
        return user;
    }
    @GetMapping
    public Collection<User> getAllUsers() {
        return users.values();
    }
    private void validateUser(User user) {
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
}