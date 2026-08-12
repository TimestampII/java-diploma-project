package ru.practicum.ewm.event;

import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.request.model.Request;
import ru.practicum.ewm.request.model.RequestStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventSpecifications {

    private EventSpecifications() {
    }

    public static Specification<Event> adminSearch(
            List<Long> users, List<EventState> states, List<Long> categories,
            LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (users != null && !users.isEmpty()) {
                predicates.add(root.get("initiator").get("id").in(users));
            }
            if (states != null && !states.isEmpty()) {
                predicates.add(root.get("state").in(states));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            }
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Event> publicSearch(
            String text, List<Long> categories, Boolean paid,
            LocalDateTime rangeStart, LocalDateTime rangeEnd, boolean onlyAvailable) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("state"), EventState.PUBLISHED));

            if (text != null && !text.isBlank()) {
                String pattern = "%" + text.toLowerCase() + "%";
                Predicate inAnnotation = cb.like(cb.lower(root.get("annotation")), pattern);
                Predicate inDescription = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(inAnnotation, inDescription));
            }
            if (categories != null && !categories.isEmpty()) {
                predicates.add(root.get("category").get("id").in(categories));
            }
            if (paid != null) {
                predicates.add(cb.equal(root.get("paid"), paid));
            }
            if (rangeStart != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), rangeStart));
            } else if (rangeEnd == null) {
                // Спецификация: "если в запросе не указан диапазон дат, нужно выгружать
                // события, которые произойдут позже текущей даты и времени"
                predicates.add(cb.greaterThanOrEqualTo(root.get("eventDate"), LocalDateTime.now()));
            }
            if (rangeEnd != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("eventDate"), rangeEnd));
            }

            if (onlyAvailable) {
                Subquery<Long> confirmedCountSubquery = query.subquery(Long.class);
                Root<Request> requestRoot = confirmedCountSubquery.from(Request.class);
                confirmedCountSubquery.select(cb.count(requestRoot))
                        .where(cb.equal(requestRoot.get("event"), root),
                                cb.equal(requestRoot.get("status"), RequestStatus.CONFIRMED));

                Predicate noLimit = cb.equal(root.get("participantLimit"), 0);
                Predicate underLimit = cb.lessThan(confirmedCountSubquery, root.get("participantLimit"));
                predicates.add(cb.or(noLimit, underLimit));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
