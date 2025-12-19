
package com.vivekanand.manager.events;

import com.vivekanand.manager.events.dto.EventCreateRequest;
import com.vivekanand.manager.events.dto.EventUpdateRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class EventService {
    private final EventRepository repo;
    private final EventParticipantRepository partRepo;

    public EventService(EventRepository r, EventParticipantRepository p) {
        repo = r;
        partRepo = p;
    }

    public Event create(Event e) {
        return repo.save(e);
    }

    public Event update(Long id, Event e) {
        var ex = repo.findById(id).orElseThrow();
        ex.setName(e.getName());
        ex.setDescription(e.getDescription());
        ex.setStartTime(e.getStartTime());
        ex.setEndTime(e.getEndTime());
        ex.setLocation(e.getLocation());
        return repo.save(ex);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Event get(Long id) {
        return repo.findById(id).orElseThrow();
    }

    public List<Event> list() {
        return repo.findAll();
    }

    public EventParticipant addParticipant(Long eventId, EventParticipant p) {
        p.setEvent(repo.findById(eventId).orElseThrow());
        return partRepo.save(p);
    }

    public List<EventParticipant> participants(Long eventId) {
        return partRepo.findByEventId(eventId);
    }

    public void removeParticipant(Long participantId) {
        partRepo.deleteById(participantId);
    }

    public Page<Event> page(String q, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        if (q != null && !q.isBlank()) return repo.findByNameContainingIgnoreCase(q, pageable);
        return repo.findAll(pageable);
    }

    public Event createFromDto(EventCreateRequest req) {
        var e = new Event();
        e.setName(req.name());
        e.setDescription(req.description());
        e.setLocation(req.location());
        // map date + optional time -> LocalDateTime
        LocalTime startT = req.startTime() != null ? req.startTime() : LocalTime.of(9,0);
        LocalTime endT   = req.endTime()   != null ? req.endTime()   : startT.plusHours(2);
        e.setStartTime(LocalDateTime.of(req.startDate(), startT));
        e.setEndTime(LocalDateTime.of(req.endDate(), endT));
        return repo.save(e);
    }

    public Event updateFromDto(Long id, EventUpdateRequest req){
        var ex = repo.findById(id).orElseThrow();
        ex.setName(req.name());
        ex.setDescription(req.description());
        ex.setLocation(req.location());
        LocalTime startT = req.startTime() != null ? req.startTime() : LocalTime.of(9,0);
        LocalTime endT   = req.endTime()   != null ? req.endTime()   : startT.plusHours(2);
        ex.setStartTime(LocalDateTime.of(req.startDate(), startT));
        ex.setEndTime(LocalDateTime.of(req.endDate(), endT));
        return repo.save(ex);
    }
}
