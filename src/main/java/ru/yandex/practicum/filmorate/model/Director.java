package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Director {
    @NotBlank(message = "ID режиссёра должен быть указан")
    private Long id;
    @NotBlank(message = "Имя режиссёра не может быть пустым")
    private String name;
}
