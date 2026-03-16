package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FilmService {

    private static final LocalDate EARLIEST_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    private final MpaService mpaService;
    private final GenreService genreService;

    public Collection<Film> findAllFilm() {
        log.info("Запрос на получение всех фильмов");
        return filmStorage.findAllFilm();
    }

    public Film getFilmById(Long id) {
        log.info("Запрос фильма с id={}", id);
        return filmStorage.getFilmById(id)
                .orElseThrow(() -> {
                    log.warn("Фильм с id={} не найден", id);
                    return new NotFoundException("Фильм с id=" + id + " не найден");
                });
    }

    public Film createFilm(Film film) {
        if (film.getMpa().getId() == null) {
            throw new ValidationException("ID рейтинга (mpa.id) должен быть указан");
        }

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (var genre : film.getGenres()) {
                if (genre.getId() == null) {
                    throw new ValidationException("ID жанра должен быть указан");
                }
                genreService.getGenreById(genre.getId());
            }
        }

        mpaService.getMpaById(film.getMpa().getId());

        validateFilm(film);
        log.info("Создание фильма: {}", film.getName());
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("ID фильма должен быть указан");
        }
        // Проверяем, что фильм существует
        if (filmStorage.getFilmById(newFilm.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + newFilm.getId() + " не найден");
        }
        validateFilm(newFilm);  //
        log.info("Обновление фильма: id={}", newFilm.getId());
        return filmStorage.updateFilm(newFilm);
    }

    public void likeFilm(Long filmId, Long userId) {
        log.info("Запрос на лайк: пользователь {} → фильм {}", userId, filmId);

        // 🔹 Проверка существования сущностей
        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        filmStorage.likeFilm(filmId, userId);
        log.info("Пользователь {} лайкнул фильм {}", userId, filmId);
    }

    public void deleteLike(Long filmId, Long userId) {
        log.info("Запрос на удаление лайка: пользователь {} → фильм {}", userId, filmId);

        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }

        filmStorage.deleteLike(filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> mostPopularFilms(Integer count) {

        log.info("Запрос на получение {} популярных фильмов", count);
        return filmStorage.mostPopularFilms(count);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null");
        }
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null) {
            throw new ValidationException("Описание не может быть пустым");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза не может быть пустой");
        }
        if (film.getReleaseDate().isBefore(EARLIEST_RELEASE_DATE)) {
            log.warn("Ошибка валидации: релиз фильма раньше 28 декабря 1895 года");
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            log.warn("Ошибка валидации: продолжительность фильма <= 0: {}", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }
}