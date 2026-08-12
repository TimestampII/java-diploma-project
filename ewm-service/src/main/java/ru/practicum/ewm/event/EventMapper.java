package ru.practicum.ewm.event;

import ru.practicum.ewm.category.Category;
import ru.practicum.ewm.category.CategoryMapper;
import ru.practicum.ewm.event.dto.EventFullDto;
import ru.practicum.ewm.event.dto.EventShortDto;
import ru.practicum.ewm.event.dto.LocationDto;
import ru.practicum.ewm.event.dto.NewEventDto;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.Location;
import ru.practicum.ewm.user.User;
import ru.practicum.ewm.user.UserMapper;

public class EventMapper {

    private EventMapper() {
    }

    public static Event toEvent(NewEventDto dto, Category category, User initiator) {
        return Event.builder()
                .title(dto.getTitle())
                .annotation(dto.getAnnotation())
                .description(dto.getDescription())
                .category(category)
                .initiator(initiator)
                .location(Location.builder()
                        .lat(dto.getLocation().getLat())
                        .lon(dto.getLocation().getLon())
                        .build())
                .paid(Boolean.TRUE.equals(dto.getPaid()))
                .participantLimit(dto.getParticipantLimit() == null ? 0 : dto.getParticipantLimit())
                .requestModeration(dto.getRequestModeration() == null || dto.getRequestModeration())
                .eventDate(dto.getEventDate())
                .build();
    }

    /**
     * confirmedRequests, views и rating вычисляются вне маппера (запросы к БД / сервису
     * статистики) и передаются явно, поэтому здесь принимаются как параметры,
     * а не берутся из Event.
     */
    public static EventFullDto toEventFullDto(Event event, long confirmedRequests, long views, long rating) {
        return EventFullDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .description(event.getDescription())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .location(LocationDto.builder()
                        .lat(event.getLocation().getLat())
                        .lon(event.getLocation().getLon())
                        .build())
                .paid(event.isPaid())
                .participantLimit(event.getParticipantLimit())
                .requestModeration(event.isRequestModeration())
                .eventDate(event.getEventDate())
                .createdOn(event.getCreatedOn())
                .publishedOn(event.getPublishedOn())
                .state(event.getState().name())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .rating(rating)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, long confirmedRequests, long views, long rating) {
        return EventShortDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .annotation(event.getAnnotation())
                .category(CategoryMapper.toCategoryDto(event.getCategory()))
                .initiator(UserMapper.toUserShortDto(event.getInitiator()))
                .paid(event.isPaid())
                .eventDate(event.getEventDate())
                .confirmedRequests(confirmedRequests)
                .views(views)
                .rating(rating)
                .build();
    }
}
