package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class FilmControllerTest {

    @Autowired
    private FilmController filmController;

    @Test
    void contextLoads() {
        assertNotNull(filmController);
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
}
