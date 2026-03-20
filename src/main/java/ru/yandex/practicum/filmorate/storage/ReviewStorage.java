package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ReviewStorage {
    Review createReview(Review review);

    Review updateReview(Review newReview);

    void deleteReview(Long id);

    Optional<Review> getReviewById(Long id);

    List<Review> getReviewsByFilm(Long id, Integer count);

    Collection<Review> findAllReviews();

    void setLike(Long reviewId, Long userId, Boolean isLike);

    void removeLike(Long reviewId, Long userId);
}
