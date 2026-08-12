package ru.practicum.ewm.stats.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

public interface EndpointHitRepository extends JpaRepository<EndpointHit, Long> {

    /**
     * Статистика по всем URI (без фильтрации по списку), считая каждый запрос.
     */
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(h.ip)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<ViewStatsDto> findAllHits(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    /**
     * Статистика по всем URI (без фильтрации по списку), считая уникальные IP.
     */
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsDto> findAllUniqueHits(@Param("start") LocalDateTime start,
                                          @Param("end") LocalDateTime end);

    /**
     * Статистика по заданному списку URI, считая каждый запрос.
     */
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(h.ip)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(h.ip) desc")
    List<ViewStatsDto> findHitsByUris(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("uris") List<String> uris);

    /**
     * Статистика по заданному списку URI, считая уникальные IP.
     */
    @Query("select new ru.practicum.ewm.stats.dto.ViewStatsDto(h.app, h.uri, count(distinct h.ip)) " +
            "from EndpointHit h " +
            "where h.timestamp between :start and :end and h.uri in :uris " +
            "group by h.app, h.uri " +
            "order by count(distinct h.ip) desc")
    List<ViewStatsDto> findUniqueHitsByUris(@Param("start") LocalDateTime start,
                                             @Param("end") LocalDateTime end,
                                             @Param("uris") List<String> uris);
}
