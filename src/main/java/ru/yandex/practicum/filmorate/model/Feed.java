package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Feed {
    private Long eventId;
    private Long userId;
    private String eventType;
    private Long entityId;
    private String operation;
    private Long timestamp;
}