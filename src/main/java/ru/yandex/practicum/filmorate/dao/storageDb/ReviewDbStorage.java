package ru.yandex.practicum.filmorate.dao.storageDb;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dao.BaseRepository;
import ru.yandex.practicum.filmorate.dao.mappers.ReviewMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Profile("database")
public class ReviewDbStorage extends BaseRepository<Review> implements ReviewStorage {
    //Отзывы
    private static final String FIND_BY_ID = "SELECT * FROM reviews WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM reviews ORDER BY useful_count DESC FETCH FIRST ? ROWS ONLY";
    private static final String INSERT = "INSERT INTO reviews (content, is_positive, user_id, film_id, useful_count) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE reviews SET content = ?, is_positive = ? WHERE id = ?";
    private static final String DELETE_REVIEW = "DELETE FROM reviews WHERE id = ?";
    private static final String FIND_BY_FILM_ID = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful_count DESC FETCH FIRST ? ROWS ONLY";

    //Лайки
    private static final String UPSERT_LIKE = "MERGE INTO reviews_likes (review_id, user_id, is_like) KEY (user_id, review_id) VALUES (?, ?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM reviews_likes WHERE review_id = ? AND user_id = ?";
    private static final String INCREMENT_RATING = "UPDATE reviews SET useful_count = useful_count + 1 WHERE id = ?";
    private static final String DECREMENT_RATING = "UPDATE reviews SET useful_count = useful_count - 1 WHERE id = ?";
    private static final String INCREMENT_RATING_BY_2 = "UPDATE reviews SET useful_count = useful_count + 2 WHERE id = ?";
    private static final String DECREMENT_RATING_BY_2 = "UPDATE reviews SET useful_count = useful_count - 2 WHERE id = ?";
    private static final String CHECK_EXISTING_VOTE = "SELECT is_like FROM reviews_likes WHERE review_id = ? AND user_id = ?";

    private final ReviewMapper reviewMapper;

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper, ReviewMapper reviewMapper) {
        super(jdbc, mapper);
        this.reviewMapper = reviewMapper;
    }

    @Override
    public Review createReview(Review review) {
        Integer useful = review.getUseful() != null ? review.getUseful() : 0;

        long id = insert(INSERT,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId(),
                useful
        );

        review.setReviewId(id);
        review.setUseful(useful);
        return review;
    }

    @Override
    public Review updateReview(Review newReview) {

        update(UPDATE,
                newReview.getContent(),
                newReview.getIsPositive(),
                newReview.getReviewId()
        );

        return findOne(FIND_BY_ID, newReview.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    @Override
    public void deleteReview(Long id) {
        try {
            int rowsAffected = jdbc.update(DELETE_REVIEW, id);
            if (rowsAffected == 0) {
                throw new NotFoundException("Отзыв с id " + id + " не найден");
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Ошибка при работе с БД: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Review> getReviewById(Long id) {
        return findOne(FIND_BY_ID, id);
    }

    @Override
    public List<Review> getReviewsByFilm(Long id, Integer count) {
        if (id != null) {
            return jdbc.query(FIND_BY_FILM_ID, reviewMapper, id, count);
        } else {
            return jdbc.query(FIND_ALL, reviewMapper, count);
        }
    }

    @Override
    public Collection<Review> findAllReviews() {
        List<Review> reviews = findMany(FIND_ALL);
        return reviews;
    }

    @Override
    public void setLike(Long reviewId, Long userId, Boolean isLike) {
        Boolean vote = getExistingVote(reviewId, userId);

        jdbc.update(UPSERT_LIKE, reviewId, userId, isLike);

        if (vote == null) {
            if (isLike) {
                jdbc.update(INCREMENT_RATING, reviewId);
            } else {
                jdbc.update(DECREMENT_RATING, reviewId);
            }
        } else if (!vote.equals(isLike)) {
            if (isLike) {
                jdbc.update(INCREMENT_RATING_BY_2, reviewId);
            } else {
                jdbc.update(DECREMENT_RATING_BY_2, reviewId);
            }
        }
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        Boolean vote = getExistingVote(reviewId, userId);
        if (vote == null) return;

        jdbc.update(DELETE_LIKE, reviewId, userId);

        if (vote != null) {
            if (vote) {
                jdbc.update(DECREMENT_RATING, reviewId); // -1
            } else {
                jdbc.update(INCREMENT_RATING, reviewId); // +1
            }
        }
    }

    public List<Review> findAll() {
        return findMany(FIND_ALL);
    }

    public Optional<Review> findById(Long id) {
        return findOne(FIND_BY_ID, id);
    }

    private Boolean getExistingVote(Long reviewId, Long userId) {
        try {
            return jdbc.queryForObject(CHECK_EXISTING_VOTE, Boolean.class, reviewId, userId);
        } catch (EmptyResultDataAccessException e) {
            log.debug("Голос не найден: reviewId={}, userId={}", reviewId, userId);
            return null;
        }
    }
//    Если вернуть false вместо null, мы не сможем отличить нового пользователя от пользователя с дизлайком, что приведёт к некорректному начислению рейтинга (2 вместо 1 при первом голосовании)
//    Нам важно различать три состояния: лайк (true), дизлайк (false) и отсутствие голоса (null)
}