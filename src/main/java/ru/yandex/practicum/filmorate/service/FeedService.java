package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FeedStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeedService {

    private final FeedStorage feedStorage;
    private final UserService userService;

    public Collection<Feed> findFeed(Long id) {

//        Collection<User> friends = userService.listFriends(id);
//
//
//
//        if (friends.isEmpty()) {
//            return Collections.emptyList();
//        }
//        List<Long> friendsId = friends.stream()
//                .map(User::getId)
//                .toList();
        return feedStorage.findFeed(id);
    }
}
