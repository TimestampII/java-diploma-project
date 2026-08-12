package ru.practicum.ewm.rating.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.user.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_ratings", uniqueConstraints = {
        // Один пользователь — один голос на событие (лайк или дизлайк, не оба сразу).
        @UniqueConstraint(name = "uq_event_rating", columnNames = {"event_id", "user_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "value", nullable = false, length = 20)
    private RatingValue value;

    @Column(name = "created", nullable = false)
    private LocalDateTime created;
}
