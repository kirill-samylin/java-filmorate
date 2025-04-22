package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final Map<Integer, Film> films = new HashMap<>();
    private int nextId = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @PostMapping
    public Film addFilm(@RequestBody Film film) {
        validateFilm(film);
        film.setId(nextId++);
        films.put(film.getId(), film);
        log.info("Добавлен новый фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film film) {
        validateFilm(film);
        if (!films.containsKey(film.getId())) {
            String message = "Фильм с id=" + film.getId() + " не найден.";
            log.warn(message);
            throw new NoSuchElementException(message);
        }
        films.put(film.getId(), film);
        log.info("Фильм обновлён: {}", film);
        return film;
    }

    @GetMapping
    public Collection<Film> getAllFilms() {
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            String error = "Название фильма не может быть пустым";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            String error = "Описание не должно превышать 200 символов";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            String error = "Дата релиза не может быть раньше 28 декабря 1895 года";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
        if (film.getDuration() <= 0) {
            String error = "Продолжительность фильма должна быть положительной";
            log.warn("Ошибка валидации: {}", error);
            throw new ValidationException(error);
        }
    }
}
