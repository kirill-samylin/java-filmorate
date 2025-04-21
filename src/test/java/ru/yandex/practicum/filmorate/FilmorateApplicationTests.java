package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

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
