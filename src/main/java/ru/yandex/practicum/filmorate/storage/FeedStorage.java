package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;
import java.util.List;

public interface FeedStorage {

    Collection<Feed> findFeed(Long id);
}
