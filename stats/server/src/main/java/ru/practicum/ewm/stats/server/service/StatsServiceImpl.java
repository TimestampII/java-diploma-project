package ru.practicum.ewm.stats.server.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.exception.InvalidRequestException;
import ru.practicum.ewm.stats.server.mapper.EndpointHitMapper;
import ru.practicum.ewm.stats.server.model.EndpointHit;
import ru.practicum.ewm.stats.server.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final EndpointHitRepository repository;

    @Override
    @Transactional
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit entity = EndpointHitMapper.toEntity(endpointHitDto);
        EndpointHit saved = repository.save(entity);
        return EndpointHitMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new InvalidRequestException("Параметры 'start' и 'end' обязательны");
        }
        if (start.isAfter(end)) {
            throw new InvalidRequestException("Дата начала диапазона не может быть позже даты окончания");
        }

        boolean urisPresent = uris != null && !uris.isEmpty();

        if (urisPresent) {
            return unique
                    ? repository.findUniqueHitsByUris(start, end, uris)
                    : repository.findHitsByUris(start, end, uris);
        }

        return unique
                ? repository.findAllUniqueHits(start, end)
                : repository.findAllHits(start, end);
    }
}
