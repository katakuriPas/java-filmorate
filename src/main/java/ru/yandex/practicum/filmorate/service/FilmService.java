package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;

    @Autowired
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

