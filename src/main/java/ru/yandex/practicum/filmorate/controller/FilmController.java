package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAllFilm() {
        return filmService.findAllFilm();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilm) {
        return filmService.updateFilm(newFilm);
    }


    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void likeFilm(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> mostPopularFilms(
            @RequestParam(defaultValue = "10")
            Integer count
    ) {
        return filmService.mostPopularFilms(count);
    }

/*
    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likeFilm(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.likeFilm(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLike(
            @PathVariable Long id,
            @PathVariable Long userId
    ) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> mostPopularFilms(
            @RequestParam(defaultValue = "10")
            Integer count
    ) {
        return filmService.mostPopularFilms(count);
    }
    */
}
