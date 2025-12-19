
// src/main/java/com/vivekanand/manager/members/MemberController.java
package com.vivekanand.manager.members;

import com.vivekanand.manager.audit.Auditable;
import com.vivekanand.manager.finance.ReportService;
import com.vivekanand.manager.members.dto.CreateMemberRequest;
import com.vivekanand.manager.members.dto.UpdateMemberRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService svc;

    public MemberController(MemberService svc) {
        this.svc = svc;
    }

    @GetMapping("/page")
    public Page<Member> page(@RequestParam(required = false) String q,
                             @RequestParam(defaultValue = "true") boolean onlyActive,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size) {
        return svc.page(q, onlyActive, page, size);
    }

    @GetMapping("/{id}")
    public Member get(@PathVariable Long id) {
        return svc.get(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "CREATE_MEMBER", entity = "Member")
    public Member create(@Valid @RequestBody CreateMemberRequest req) {
        return svc.create(req);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "UPDATE_MEMBER", entity = "Member")
    public Member update(@PathVariable Long id, @Valid @RequestBody UpdateMemberRequest req) {
        return svc.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Auditable(action = "DELETE_MEMBER", entity = "Member")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        svc.delete(id);
        return ResponseEntity.noContent().build();
    }

    // PDF report (admin only)
    @GetMapping(value = "/report", produces = MediaType.APPLICATION_PDF_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<byte[]> membersPdf(ReportService reports) {
        byte[] pdf = reports.membersReport(svc.page(null, false, 0, Integer.MAX_VALUE).getContent());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=members_report.pdf")
                .body(pdf);
    }

    public static record MemberLookup(Long id, String fullName, String phone, String email) {
    }

    @GetMapping("/lookup")
    public List<MemberLookup> lookup(@RequestParam String q) {
        var page = svc.page(q, false, 0, 10); // first 10 matches by name/email
        return page.getContent().stream()
                .map(m -> new MemberLookup(m.getId(), m.getFullName(), m.getPhone(), m.getEmail()))
                .collect(Collectors.toList());
    }

}
