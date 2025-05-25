package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService; // Для проверки существования пользователя
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Film addFilm(Film film) {
        validateFilm(film);
        log.info("Добавлен новый фильм: {}", film);
        return filmStorage.addFilm(film);
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    public Film updateFilm(Film film) {
        validateFilm(film);
        Film existingFilm = filmStorage.getFilmById(film.getId());
        if (existingFilm == null) {
            String message = "Фильм с id=" + film.getId() + " не найден.";
            log.warn(message);
            throw new NoSuchElementException(message);
        }
        log.info("Фильм обновлён: {}", film);
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.getAllFilms();
    }

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NoSuchElementException("Фильм с id " + filmId + " не найден");
        }
        if (userService.getUserById(userId) == null) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }

        film.addLike(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.getFilmById(filmId);
        if (film == null) {
            throw new NoSuchElementException("Фильм с id " + filmId + " не найден");
        }
        if (userService.getUserById(userId) == null) {
            throw new NoSuchElementException("Пользователь с id " + userId + " не найден");
        }

        film.removeLike(userId);
    }

    public List<Film> getPopularFilms(int count) {
        return filmStorage.getAllFilms().stream()
                .sorted(Comparator.comparingInt(f -> -f.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
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
