package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.storage.FeedStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;


@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserStorage userStorage;

    public Collection<Feed> findFeed(Long id) {
        log.info("Получен запрос на получение дейстий пользователя {}", id);
        Collection<Feed> feedUser = feedStorage.findFeed(id);
        log.info("Запрос на получение действий пользователя {} успешно обработан", id);
        return feedUser;
    }
}

//Логика по ТЗ:
//private final UserService userService;
//Collection<User> friends = userService.listFriends(id);
//        if (friends.isEmpty()) {
//        return Collections.emptyList();
//        }
//List<Long> friendsId = friends.stream()
//        .map(User::getId)
//        .toList();