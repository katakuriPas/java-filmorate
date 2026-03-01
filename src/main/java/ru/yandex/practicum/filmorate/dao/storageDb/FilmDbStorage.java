package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.dao.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.dao.mappers.MpaMapper;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Component
@Profile("database")
@RequiredArgsConstructor
@Slf4j
public class FilmDbStorage implements FilmStorage {
    private static final int LIMIT_FILMS = 10;

    private static final String FIND_BY_ID =
            "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "WHERE f.id = ?";

    private static final String FIND_ALL =
            "SELECT f.*, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id";

    private static final String INSERT = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String LIKE_FILM = "INSERT INTO like_films (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM like_films WHERE user_id = ? AND film_id = ?";

    private static final String MOST_POPULAR_FILMS =
            "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name as mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.mpa_id = m.id " +
                    "ORDER BY (SELECT COUNT(*) FROM like_films lf WHERE lf.film_id = f.id) DESC " +
                    "FETCH FIRST ? ROWS ONLY";

    private static final String FIND_GENRES_BY_FILM_ID =
            "SELECT g.* FROM genre g " +
                    "JOIN film_genres fg ON g.id = fg.genre_id " +
                    "WHERE fg.film_id = ?";

    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";

    private final JdbcTemplate jdbc;
    private final RowMapper<Film> mapper;
    private final MpaMapper mpaMapper;
    private final GenreMapper genreMapper;
    private final FilmMapper filmMapper;

    @Override
    public Optional<Film> getFilmById(Long id) {
        Optional<Film> filmOptional = findOne(FIND_BY_ID, id);

        if (filmOptional.isPresent()) {
            Film film = filmOptional.get();

            List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, genreMapper, id);
            film.setGenres(new HashSet<>(genres));

            return Optional.of(film);
        }
        return Optional.empty();
    }

    @Override
    public Collection<Film> findAllFilm() {
        List<Film> films = findMany(FIND_ALL);

        for (Film film : films) {
            List<Genre> genres = jdbc.query(FIND_GENRES_BY_FILM_ID, genreMapper, film.getId());
            film.setGenres(new HashSet<>(genres));
        }
        return films;
    }

    @Override
    public void likeFilm(Long filmId, Long userId) {
        jdbc.update(LIKE_FILM, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, userId, filmId);
    }

    @Override
    public List<Film> mostPopularFilms(Integer count) {
        return jdbc.query(MOST_POPULAR_FILMS, filmMapper, Objects.requireNonNullElse(count, LIMIT_FILMS));
    }

    @Override
    public Film createFilm(Film film) {
        long id = insert(INSERT,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId()
        );
        saveFilmGenres(id, film.getGenres());

        film.setId(id);
        return film;
    }

    @Override
    public Film updateFilm(Film newFilm) {
        update(UPDATE,
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getReleaseDate(),
                newFilm.getDuration(),
                newFilm.getMpa().getId(),
                newFilm.getId()
        );

        saveFilmGenres(newFilm.getId(), newFilm.getGenres());

        return getFilmById(newFilm.getId())
                .orElseThrow(() -> new InternalServerException("Не удалось получить обновлённый фильм"));
    }

    @Override
    public Optional<Mpa> getMpaByName(String name) {
        String sql = "SELECT * FROM mpa WHERE name = ?";
        try {
            return Optional.ofNullable(jdbc.queryForObject(sql, mpaMapper, name));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private void saveFilmGenres(Long filmId, Set<Genre> genres) {
        jdbc.update(DELETE_FILM_GENRES, filmId);

        // Добавляем новые
        if (genres != null && !genres.isEmpty()) {
            for (Genre genre : genres) {
                if (genre.getId() != null) {
                    jdbc.update(INSERT_FILM_GENRE, filmId, genre.getId());
                }
            }
        }
    }

    private Optional<Film> findOne(String query, Object... params) {
        try {
            Film result = jdbc.queryForObject(query, mapper, params);
            return Optional.ofNullable(result);
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    private List<Film> findMany(String query, Object... params) {
        return jdbc.query(query, mapper, params);
    }

    private boolean delete(String query, Object... params) {
        int rowsDeleted = jdbc.update(query, params);
        return rowsDeleted > 0;
    }

    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new InternalServerException("Не удалось обновить данные");
        }
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKeyAs(Long.class);
        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}