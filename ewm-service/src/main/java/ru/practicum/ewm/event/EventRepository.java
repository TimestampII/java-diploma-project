package ru.practicum.ewm.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import ru.practicum.ewm.event.model.Event;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    Page<Event> findByInitiatorId(Long initiatorId, Pageable pageable);

    boolean existsByCategoryId(Long categoryId);

    /**
     * Ищет событие сразу с проверкой владельца на уровне БД (WHERE event_id = ? AND
     * initiator_id = ?), а не отдельным запросом события с последующей проверкой
     * в Java — ошибка "не найдено" будет той же самой в обоих случаях.
     */
    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);
}
