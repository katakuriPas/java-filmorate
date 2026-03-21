package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.ReviewStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    @Autowired
    private final ReviewStorage reviewStorage;

    public Review createReview(Review review) {
        Long userId = review.getUserId();
        Long filmId = review.getFilmId();

        validateReviewFormat(review);
        validateIds(filmId, userId);

        return reviewStorage.createReview(review);
    }

    public Review updateReview(Review newReview) {
        return reviewStorage.updateReview(newReview);
    }

    public void deleteReview(Long id) {
        reviewStorage.deleteReview(id);
    }

    public Optional<Review> getReviewById(Long id) {
        log.info("Запрос на получение пользователя с id: {}", id);

        Optional<Review> existing = reviewStorage.getReviewById(id);
        if (existing.isEmpty()) {
            throw new NotFoundException("Review с id = " + id + " не найден");
        }

        return reviewStorage.getReviewById(id);
    }

    public List<Review> getReviewsByFilm(Long id, int count) {
        return reviewStorage.getReviewsByFilm(id, count);
    }

    public Collection<Review> findAllReviews() {
        return reviewStorage.findAllReviews();
    }

    public void addLike(Long reviewId, Long userId) {
        validateIds(reviewId, userId);
        reviewStorage.setLike(reviewId, userId, true);
    }

    public void addDislike(Long reviewId, Long userId) {
        validateIds(reviewId, userId);
        reviewStorage.setLike(reviewId, userId, false);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateIds(reviewId, userId);
        reviewStorage.removeLike(reviewId, userId);
    }

    public void removeDislike(Long reviewId, Long userId) {
        removeLike(reviewId, userId);  // Удаление дизлайка = удаление голоса
    }


    private void validateReviewFormat(Review review) {
        if (review.getContent() == null || review.getContent().isBlank()) {
            throw new ValidationException("Отзыв не должен быть пустым");
        }

        if (review.getIsPositive() == null) {
            throw new ValidationException("Тип отзыва не должен быть пустым");
        }

        if (review.getUserId() == null) {
            throw new ValidationException("Id пользователя не указано");
        }

        if (review.getFilmId() == null) {
            throw new ValidationException("Id фильма не указано");
        }
    }

    private void validateIds(Long filmId, Long userId) {
        if (filmId == null || filmId <= 0) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (userId == null || userId <= 0) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }
}
