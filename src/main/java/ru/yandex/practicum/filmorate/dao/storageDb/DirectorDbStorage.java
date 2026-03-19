package ru.yandex.practicum.filmorate.dao.storageDb;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mappers.DirectorMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;

@Component
@Profile("database")
public class DirectorDbStorage extends BaseRepository<Director> {

    private static final String FIND_BY_ID = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM directors ORDER BY id";
    private static final String INSERT = "INSERT INTO directors (name) VALUES (?)";
    private static final String UPDATE = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM directors WHERE id = ?";

    public DirectorDbStorage(JdbcTemplate jdbc, DirectorMapper mapper) {
        super(jdbc, mapper);
    }

    public Optional<Director> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }

    public List<Director> findAll() {
        return findMany(FIND_ALL);
    }

    public Director create(Director director) {
        long id = insert(INSERT, director.getName());
        director.setId(id);
        return director;
    }

    public Director update(Director director) {
        update(UPDATE, director.getName(), director.getId());
        return director;
    }

    public void delete(Long id) {
        delete(DELETE, id);
    }
}