package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class LikeFilms {
    private Long film_id;
    private Long user_id;
}
