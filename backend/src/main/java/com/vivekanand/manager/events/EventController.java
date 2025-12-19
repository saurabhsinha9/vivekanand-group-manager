
package com.vivekanand.manager.events;

import com.vivekanand.manager.audit.Auditable;
import com.vivekanand.manager.events.dto.AddParticipantRequest;
import com.vivekanand.manager.events.dto.EventCreateRequest;
import com.vivekanand.manager.events.dto.EventUpdateRequest;
import com.vivekanand.manager.events.dto.ParticipantView;
import com.vivekanand.manager.finance.ReportService;
import com.vivekanand.manager.members.Member;
import com.vivekanand.manager.members.MemberRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService svc;
    private final EventRepository repo;
    private final MemberRepository memberRepo;

    public EventController(EventService s, EventRepository r, MemberRepository mr) {
        this.svc = s;
        this.repo = r;
        this.memberRepo = mr;
    }

    @GetMapping
    public List<Event> list() {
        return svc.list();
    }

    @GetMapping("/{id}")
    public Event get(@PathVariable Long id) {
        return svc.get(id);
    }

    @GetMapping("/page")
    public Page<Event> page(@RequestParam(required = false) String q, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return svc.page(q, page, size);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "CREATE_EVENT", entity = "Event")
    public Event create(@Valid @RequestBody EventCreateRequest req) {
        return svc.createFromDto(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "UPDATE_EVENT", entity = "Event")
    public Event update(@PathVariable Long id, @Valid @RequestBody EventUpdateRequest req) {
        return svc.updateFromDto(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "DELETE_EVENT", entity = "Event")
    public void delete(@PathVariable Long id) {
        svc.delete(id);
    }

    @PostMapping("/{id}/participants")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Auditable(action = "ADD_PARTICIPANT", entity = "EventParticipant")
    public EventParticipant addParticipant(@PathVariable Long id, @RequestBody AddParticipantRequest req) {
        Long resolvedMemberId = req.memberId();

        if (resolvedMemberId == null) {
            String key = req.memberExternalKey();
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException("Provide either memberId or memberExternalKey (email/phone)");
            }

            // Try email first (case-insensitive)
            var emailMatch = memberRepo.findByEmailIgnoreCase(key);
            if (emailMatch.isPresent()) {
                resolvedMemberId = emailMatch.get().getId();
            } else {
                // Then phone (exact)
                var phoneMatch = memberRepo.findByPhone(key);
                if (phoneMatch.isPresent()) {
                    resolvedMemberId = phoneMatch.get().getId();
                } else {
                    throw new java.util.NoSuchElementException("Member not found for external key: " + key);
                }
            }
        }

        var participant = new EventParticipant();
        participant.setEvent(svc.get(id));
        participant.setMemberId(resolvedMemberId);
        participant.setRole(req.role());
        return svc.addParticipant(id, participant);
    }

    @GetMapping("/{id}/participants")
    public List<EventParticipant> participants(@PathVariable Long id) {
        return svc.participants(id);
    }

    @DeleteMapping("/participants/{participantId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Auditable(action = "REMOVE_PARTICIPANT", entity = "EventParticipant")
    public ResponseEntity<Void> removeParticipant(@PathVariable Long participantId) {
        svc.removeParticipant(participantId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/{id}/participants/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> participantsPdf(@PathVariable Long id, ReportService reports) {
        var e = svc.get(id);
        var parts = svc.participants(id);
        var memberIds = parts.stream().map(EventParticipant::getMemberId).collect(Collectors.toSet());
        var members = memberRepo.findAll().stream()
                .filter(m -> memberIds.contains(m.getId()))
                .collect(Collectors.toMap(Member::getId, m -> m));
        List<ParticipantView> pv = parts.stream().map(p ->
                        new ParticipantView(
                                p.getId(),
                                p.getMemberId(),
                                members.getOrDefault(p.getMemberId(), null) != null ? members.get(p.getMemberId()).getFullName() : ("#" + p.getMemberId()),
                p.getRole()
        )
     ).collect(java.util.stream.Collectors.toList());
        byte[] pdf = reports.participantsNameRoleReport(e.getName(), pv);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=participants_event_" + id + ".pdf").body(pdf);
    }
    @GetMapping("/{id}/participants/view")
    public List<ParticipantView> participantsView(@PathVariable Long id){
        var parts = svc.participants(id);
        var memberIds = parts.stream().map(EventParticipant::getMemberId).collect(java.util.stream.Collectors.toSet());
        var members = memberRepo.findAll().stream()
                .filter(m -> memberIds.contains(m.getId()))
                .collect(java.util.stream.Collectors.toMap(com.vivekanand.manager.members.Member::getId, m -> m));
        return parts.stream().map(p -> new ParticipantView(
                        p.getId(),
                        p.getMemberId(),
                        members.getOrDefault(p.getMemberId(), null) != null ? members.get(p.getMemberId()).getFullName() : ("#" + p.getMemberId()),
                        p.getRole()
        )
        ).collect(Collectors.toList());
    }
}
