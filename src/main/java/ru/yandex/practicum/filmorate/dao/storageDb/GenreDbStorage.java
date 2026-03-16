package ru.yandex.practicum.filmorate.dao.storageDb;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
public class GenreDbStorage extends BaseRepository<Genre> {

    private static final String FIND_BY_ID = "SELECT * FROM genre WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM genre";

    public GenreDbStorage(JdbcTemplate jdbc, GenreMapper genreMapper) {
        super(jdbc, genreMapper);
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Genre> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }
}