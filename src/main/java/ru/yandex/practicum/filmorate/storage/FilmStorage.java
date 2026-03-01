package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAllFilm();

    Film createFilm(Film film);

    Film updateFilm(Film newFilm);

    Optional<Film> getFilmById(Long id);

    void likeFilm(Long id, Long userId);

    void deleteLike(Long id, Long userId);

    List<Film> mostPopularFilms(Integer count);

    Optional<Mpa> getMpaByName(String name);
}
