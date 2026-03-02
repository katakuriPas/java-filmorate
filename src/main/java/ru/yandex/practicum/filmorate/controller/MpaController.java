package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {

    private final MpaService mpaService;

    @GetMapping
    public List<Mpa> getAllGenres() {
        return mpaService.getAllMpa();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mpa getGenreById(@PathVariable Long id) {
        return mpaService.getMpaById(id);
    }
}
