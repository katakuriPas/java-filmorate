package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
@RequiredArgsConstructor
public class MpaDbStorage {
    private static final String FIND_BY_ID = "SELECT * FROM mpa WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM mpa";

    private final JdbcTemplate jdbc;
    private final MpaMapper mpaMapper;

    public List<Mpa> findAll() {
        return jdbc.query(FIND_ALL, mpaMapper);
    }

    public Optional<Mpa> findById(Long id) {
        try {
            Mpa mpa = jdbc.queryForObject(FIND_BY_ID, mpaMapper, id);
            return Optional.ofNullable(mpa);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
