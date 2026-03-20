package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Data
@Slf4j
@Builder
public class Feed {
    private Long eventId;
    private Long userId;
    private String eventType;
    private Long entityId;
    private String operation;
    private LocalDateTime timestamp;
}