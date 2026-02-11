package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> findAllFilm() {
        return filmStorage.findAllFilm();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.updateFilm(newFilm);
    }

    public void likeFilm(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);

        // Даже с @Builder.Default инициализация не проходит, выручило только это
        if (film.getIdUsersLike() == null) {
            film.setIdUsersLike(new HashSet<>());
        }
        if (user.getLikeFilms() == null) {
            user.setLikeFilms(new HashSet<>());
        }

        user.getLikeFilms().add(id);
        film.getIdUsersLike().add(userId);
        filmStorage.updateFilm(film);
    }

    public void deleteLike(Long id, Long userId) {
        Film film = filmStorage.getFilmById(id);
        User user = userStorage.getUserById(userId);

        user.getLikeFilms().remove(id);
        film.getIdUsersLike().remove(userId);
        filmStorage.updateFilm(film);
    }

    public List<Film> mostPopularFilms(Integer count) {
        return filmStorage.findAllFilm().stream()
                .sorted(Comparator.comparing(Film::getSumLikes).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }
}

