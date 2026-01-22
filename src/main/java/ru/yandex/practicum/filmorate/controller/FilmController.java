package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAllFilm() {
        log.info("Запрос на получение всех фильмов. Количество: {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film createFilm (@RequestBody Film film) {
        log.info("Попытка создания фильма: {}", film.getName());

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() == null ||film.getDescription().length() > 200) {
            log.warn("Ошибка валидации: описание больше 200 символов или null");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (!film.getReleaseDate().isAfter(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: релиз фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("Ошибка валидации: продолжительность фильма <= 0: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Фильм успешно добавлен: {}", film.getName());
        return film;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        log.info("Редактирование фильма: {}", newFilm.getName());

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        if (newFilm.getId() == null) {
            log.warn("Id не был указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }
        if (newFilm.getDescription().length() > 200) {
            log.warn("Ошибка валидации: описание больше 200 символов");
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (newFilm.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: некорректная дата релиза");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (newFilm.getDuration() <= 0) {
            log.warn("Ошибка валидации: продолжительность <= 0 при обновлении");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }

        Film existingFilm = films.get(newFilm.getId());
        existingFilm.setName(newFilm.getName());
        existingFilm.setDescription(newFilm.getDescription());
        existingFilm.setReleaseDate(newFilm.getReleaseDate());
        existingFilm.setDuration(newFilm.getDuration());

        log.info("Фильм с id = {} успешно обновлён", newFilm.getId());
        return existingFilm;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0 );
        return ++currentMaxId;
    }
}
