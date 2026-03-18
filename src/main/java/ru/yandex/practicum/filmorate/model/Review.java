package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@Builder
public class Review {
    private Long reviewId;

    @NotBlank(message = "Отзыв не должен быть пустым")
    private String content;

    @NotNull(message = "Тип отзыва не должен быть пустым")
    private Boolean isPositive;

    @NotNull(message = "Id пользователя не указано")
    @Min(value = 1, message = "Id пользователя должен быть положительным")
    private Long userId;

    @NotNull(message = "Id фильма не указано")
    @Min(value = 1, message = "Id фильма должен быть положительным")
    private Long filmId;

    @Builder.Default
    private Integer useful = 0;

}
