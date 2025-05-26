package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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

    @Test
    void addFriendAndGetFriendsShouldSucceed() {
        // Создаём двух пользователей
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setLogin("alice");
        user1.setName("Alice");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setLogin("bob");
        user2.setName("Bob");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        user1 = userController.createUser(user1);
        user2 = userController.createUser(user2);

        // Добавляем в друзья
        userController.addFriend(user1.getId(), user2.getId());

        // user1 получает список друзей
        List<User> friends1 = userController.getFriends(user1.getId());
        assertEquals(1, friends1.size());
        assertEquals(user2.getId(), friends1.getFirst().getId());

        // user2 получает список друзей (взаимная дружба)
        List<User> friends2 = userController.getFriends(user2.getId());
        assertEquals(1, friends2.size());
        assertEquals(user1.getId(), friends2.getFirst().getId());
    }

    @Test
    void removeFriendShouldClearBothSides() {
        // Создаём двух пользователей
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setLogin("alice");
        user1.setName("Alice");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setLogin("bob");
        user2.setName("Bob");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        user1 = userController.createUser(user1);
        user2 = userController.createUser(user2);

        userController.addFriend(user1.getId(), user2.getId());

        // Удаляем дружбу
        userController.removeFriend(user1.getId(), user2.getId());

        List<User> friends1 = userController.getFriends(user1.getId());
        List<User> friends2 = userController.getFriends(user2.getId());
        assertTrue(friends1.isEmpty());
        assertTrue(friends2.isEmpty());
    }

    @Test
    void getCommonFriendsShouldReturnCorrectResult() {
        // user1 и user2 оба добавляют user3 в друзья
        // Создаём двух пользователей
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setLogin("alice");
        user1.setName("Alice");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setLogin("bob");
        user2.setName("Bob");
        user2.setBirthday(LocalDate.of(1991, 2, 2));


        User user3 = new User();
        user3.setEmail("three@example.com");
        user3.setLogin("three");
        user3.setName("Three");
        user3.setBirthday(LocalDate.of(1993, 3, 3));

        user1 = userController.createUser(user1);
        user2 = userController.createUser(user2);
        user3 = userController.createUser(user3);

        userController.addFriend(user1.getId(), user3.getId());
        userController.addFriend(user2.getId(), user3.getId());

        List<User> common = userController.getCommonFriends(user1.getId(), user2.getId());
        assertEquals(1, common.size());
        assertEquals(user3.getId(), common.getFirst().getId());
    }

    @Test
    void getFriendsWhenNoneShouldReturnEmptyList() {
        User user = new User();
        user.setEmail("a@example.com");
        user.setLogin("alice");
        user.setName("Alice");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        user = userController.createUser(user);

        List<User> friends = userController.getFriends(user.getId());
        assertTrue(friends.isEmpty());
    }

    @Test
    void getCommonFriendsWhenNoneShouldReturnEmptyList() {
        User user1 = new User();
        user1.setEmail("a@example.com");
        user1.setLogin("alice");
        user1.setName("Alice");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setEmail("b@example.com");
        user2.setLogin("bob");
        user2.setName("Bob");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        user1 = userController.createUser(user1);
        user2 = userController.createUser(user2);
        List<User> commonFriends = userController.getCommonFriends(user1.getId(), user2.getId());
        assertTrue(commonFriends.isEmpty());
    }
}
