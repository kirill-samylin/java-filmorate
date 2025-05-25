package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Autowired
    private UserController userController;

    @Test
    void contextLoads() {
        assertNotNull(filmController);
        assertNotNull(userController);
    }

    @Test
    void addValidFilmShouldSucceed() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Sci-fi thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film result = filmController.addFilm(film);
        assertEquals("Inception", result.getName());
    }

    @Test
    void addFilmWithBlankNameShouldFail() {
        Film film = new Film();
        film.setName("   ");
        film.setDescription("Test");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        assertEquals("Название фильма не может быть пустым", ex.getMessage());
    }

    @Test
    void addFilmWithTooOldDateShouldFail() {
        Film film = new Film();
        film.setName("Oldie");
        film.setDescription("Very old");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(120);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        assertEquals("Дата релиза не может быть раньше 28 декабря 1895 года", ex.getMessage());
    }

    @Test
    void addFilmWithNegativeDurationShouldFail() {
        Film film = new Film();
        film.setName("Negative Duration");
        film.setDescription("Oops");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(-10);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.addFilm(film));
        assertEquals("Продолжительность фильма должна быть положительной", ex.getMessage());
    }

    @Test
    void userCanLikeFilmAndSeePopularFilms() {
        // Создаём фильм
        Film film = new Film();
        film.setName("Titanic");
        film.setDescription("Epic romance");
        film.setReleaseDate(LocalDate.of(1997, 12, 19));
        film.setDuration(195);
        Film addedFilm = filmController.addFilm(film);

        // Создаём пользователя
        User user = new User();
        user.setEmail("jack@example.com");
        user.setLogin("jack123");
        user.setName("Jack");
        user.setBirthday(LocalDate.of(1980, 1, 1));
        User addedUser = userController.createUser(user);

        // Пользователь лайкает фильм
        filmController.addLike(addedFilm.getId(), addedUser.getId());

        // Получаем список популярных фильмов
        List<Film> popular = filmController.getPopularFilms(10);

        assertEquals(1, popular.size());
        assertEquals(addedFilm.getId(), popular.getFirst().getId());
        assertTrue(popular.getFirst().getLikes().contains(addedUser.getId()));
    }

    @Test
    void userCanRemoveLikeFromFilm() {
        // Создаём фильм
        Film film = new Film();
        film.setName("The Matrix");
        film.setDescription("Sci-Fi Classic");
        film.setReleaseDate(LocalDate.of(1999, 3, 31));
        film.setDuration(136);
        Film addedFilm = filmController.addFilm(film);

        // Создаём пользователя
        User user = new User();
        user.setEmail("neo@matrix.com");
        user.setLogin("neo");
        user.setName("Neo");
        user.setBirthday(LocalDate.of(1975, 5, 20));
        User addedUser = userController.createUser(user);

        // Лайк
        filmController.addLike(addedFilm.getId(), addedUser.getId());
        assertTrue(filmController.getPopularFilms(10).getFirst().getLikes().contains(addedUser.getId()));

        // Удаление лайка
        filmController.removeLike(addedFilm.getId(), addedUser.getId());
        Film updated = filmController.getPopularFilms(10).getFirst();
        assertFalse(updated.getLikes().contains(addedUser.getId()));
    }

    @Test
    void getPopularFilmsReturnsDefault10() {
        // Очистим и добавим 1 фильм
        Film film = new Film();
        film.setName("Interstellar");
        film.setDescription("Space, time, and love");
        film.setReleaseDate(LocalDate.of(2014, 11, 7));
        film.setDuration(169);
        filmController.addFilm(film);

        // По умолчанию должно вернуться 1 из 10
        List<Film> top = filmController.getPopularFilms(10);
        assertEquals(1, top.size());
    }
}
