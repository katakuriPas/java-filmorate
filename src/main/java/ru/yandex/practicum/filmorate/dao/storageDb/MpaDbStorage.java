package ru.yandex.practicum.filmorate.dao.storageDb;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
public class MpaDbStorage extends BaseRepository<Mpa> {
    private static final String FIND_BY_ID = "SELECT * FROM mpa WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM mpa";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public List<Mpa> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Mpa> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }
}