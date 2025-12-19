
package com.vivekanand.manager.events;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "event_participants")
public class EventParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;
    private Long memberId;
    private String role;
}
