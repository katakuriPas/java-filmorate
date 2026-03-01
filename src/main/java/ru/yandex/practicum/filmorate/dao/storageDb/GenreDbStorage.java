package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
@RequiredArgsConstructor
public class GenreDbStorage {
    private static final String FIND_BY_ID = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM genre";

    private final JdbcTemplate jdbc;
    private final GenreMapper genreMapper;

    public List<Genre> findAll() {
        return jdbc.query(FIND_ALL, genreMapper);
    }

    public Optional<Genre> findById(Long id) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID, genreMapper, id);
            return Optional.ofNullable(genre);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
