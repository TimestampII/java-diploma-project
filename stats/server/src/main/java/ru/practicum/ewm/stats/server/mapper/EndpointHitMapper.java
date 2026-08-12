package ru.practicum.ewm.stats.server.mapper;

import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.server.model.EndpointHit;

public class EndpointHitMapper {

    private EndpointHitMapper() {
    }

    public static EndpointHit toEntity(EndpointHitDto dto) {
        return EndpointHit.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();
    }

    public static EndpointHitDto toDto(EndpointHit entity) {
        return EndpointHitDto.builder()
                .id(entity.getId())
                .app(entity.getApp())
                .uri(entity.getUri())
                .ip(entity.getIp())
                .timestamp(entity.getTimestamp())
                .build();
    }
}
