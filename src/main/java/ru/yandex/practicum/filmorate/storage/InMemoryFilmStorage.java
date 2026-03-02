package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Profile("memory")
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public Collection<Film> findAllFilm() {
        log.info("Запрос на получение всех фильмов. Количество: {}", films.size());
        return films.values();
    }

    @Override
    public Film createFilm(Film film) {
        log.info("Попытка создания фильма: {}", film.getName());

        if (film == null) {
            throw new ValidationException("Фильм не может быть пустым");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: название фильма пустое");
            throw new ValidationException("Название не может быть пустым");
        }

        validateFilm(film);

        film.setId(getNextId());
        log.info("Фильм успешно добавлен: {}", film.getName());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        log.info("Редактирование фильма: {}", newFilm.getName());

        if (!films.containsKey(newFilm.getId())) {
            log.warn("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        if (newFilm.getId() == null) {
            log.warn("Id не был указан");
            throw new ConditionsNotMetException("Id должен быть указан");
        }

        validateFilm(newFilm);

        Film existingFilm = films.get(newFilm.getId());
        existingFilm.setName(newFilm.getName());
        existingFilm.setDescription(newFilm.getDescription());
        existingFilm.setReleaseDate(newFilm.getReleaseDate());
        existingFilm.setDuration(newFilm.getDuration());

        log.info("Фильм с id = {} успешно обновлён", newFilm.getId());
        return existingFilm;
    }

    private void validateFilm(Film film) {
        if (film.getDescription() == null) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: релиз фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() < 0) {
            log.warn("Ошибка валидации: продолжительность фильма <= 0: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

    public Optional<Film> getFilmById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void likeFilm(Long id, Long userId) {

    }

    @Override
    public void deleteLike(Long id, Long userId) {

    }

    @Override
    public List<Film> mostPopularFilms(Integer count) {
        return List.of();
    }
}
