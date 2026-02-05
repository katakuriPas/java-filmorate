package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Test
    void shouldCreateValidFilm() {
        Film film = Film.builder()
                .name("Inception")
                .description("A thief who steals corporate secrets...")
                .releaseDate(LocalDate.of(2010, 7, 16))
                .duration(148)
                .build();

        assertEquals("Inception", film.getName());
        assertEquals("A thief who steals corporate secrets...", film.getDescription());
        assertEquals(LocalDate.of(2010, 7, 16), film.getReleaseDate());
        assertEquals(148, film.getDuration());
    }

    @Test
    void shouldThrowOnEmptyName() {
        Film film = Film.builder()
                .name("")
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        assertThrows(ValidationException.class, () -> validateFilm(film));
    }

    @Test
    void shouldThrowOnNullName() {
        Film film = Film.builder()
                .name(null)
                .description("Valid description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        assertThrows(ValidationException.class, () -> validateFilm(film));
    }

    @Test
    void shouldThrowOnDescriptionTooLong() {
        String longDesc = "a".repeat(201);
        Film film = Film.builder()
                .name("Test")
                .description(longDesc)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        assertThrows(ValidationException.class, () -> validateFilm(film));
    }

    @Test
    void shouldAcceptDescriptionExactly200Chars() {
        String desc200 = "a".repeat(200);
        Film film = Film.builder()
                .name("Test")
                .description(desc200)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(100)
                .build();

        assertDoesNotThrow(() -> validateFilm(film));
    }

    @Test
    void shouldThrowOnReleaseDateBefore1895_12_28() {
        Film film = Film.builder()
                .name("Test")
                .description("OK")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(100)
                .build();

        assertThrows(ValidationException.class, () -> validateFilm(film));
    }

    @Test
    void shouldAcceptEarliestReleaseDatePlusOneDay() {
        Film film = Film.builder()
                .name("Test")
                .description("OK")
                .releaseDate(LocalDate.of(1895, 12, 29))
                .duration(100)
                .build();

        assertDoesNotThrow(() -> validateFilm(film));
    }

    @Test
    void shouldThrowOnZeroOrNegativeDuration() {
        Film film1 = Film.builder()
                .name("Test")
                .description("OK")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0)
                .build();

        Film film2 = Film.builder()
                .name("Test")
                .description("OK")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-5)
                .build();

        assertThrows(ValidationException.class, () -> validateFilm(film1));
        assertThrows(ValidationException.class, () -> validateFilm(film2));
    }

    // Вспомогательный метод, имитирующий валидацию из контроллера
    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (!film.getReleaseDate().isAfter(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

}
