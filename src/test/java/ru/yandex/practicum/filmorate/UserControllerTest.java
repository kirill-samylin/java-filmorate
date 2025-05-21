package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class UserControllerTest {

    @Autowired
    private UserController userController;

    @Test
    void contextLoads() {
        assertNotNull(userController);
    }

    @Test
    void createUserWithValidDataShouldSucceed() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user123");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 5, 5));

        User result = userController.createUser(user);
        assertEquals("Test User", result.getName());
    }

    @Test
    void createUserWithBlankLoginShouldFail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Логин не может быть пустым или содержать пробелы", ex.getMessage());
    }

    @Test
    void createUserWithEmailWithoutAtSymbolShouldFail() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("user123");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        ValidationException ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Недопустимый email", ex.getMessage());
    }

    @Test
    void createUserWithFutureBirthdayShouldFail() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("user123");
        user.setBirthday(LocalDate.now().plusDays(1));
        ValidationException ex = assertThrows(ValidationException.class, () -> userController.createUser(user));
        assertEquals("Дата рождения не может быть в будущем", ex.getMessage());
    }

    @Test
    void createUserWithEmptyNameShouldUseLoginAsName() {
        User user = new User();
        user.setEmail("name@test.com");
        user.setLogin("nickname");
        user.setName("  ");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        User result = userController.createUser(user);
        assertEquals("nickname", result.getName());
    }
}
