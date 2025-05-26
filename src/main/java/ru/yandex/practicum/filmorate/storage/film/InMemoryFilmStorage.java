package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private long nextId = 1;

    @Override
    public Film addFilm(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NoSuchElementException("Фильм с id " + film.getId() + " не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public boolean deleteFilm(Long filmId) {
        return films.remove(filmId) != null;
    }

    @Override
    public Film getFilmById(Long filmId) {
        Film film = films.get(filmId);
        if (film == null) {
            throw new NoSuchElementException("Фильм с id " + filmId + " не найден");
        }
        return film;
    }

    @Override
    public Collection<Film> getAllFilms() {
        return films.values();
    }
}