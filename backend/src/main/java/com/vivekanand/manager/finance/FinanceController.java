
package com.vivekanand.manager.finance;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {
    private final FinanceService svc;
    private final ReportService reports;
    private final FinancialRecordRepository repo;

    public FinanceController(FinanceService s, ReportService r, FinancialRecordRepository repo) {
        svc = s;
        reports = r;
        this.repo = repo;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public FinancialRecord add(@RequestBody FinancialRecord r) {
        return svc.add(r);
    }

    @GetMapping("/event/{eventId}")
    public java.util.List<FinancialRecord> byEvent(@PathVariable Long eventId) {
        return svc.listByEvent(eventId);
    }

    @GetMapping("/event/{eventId}/balance")
    public String balance(@PathVariable Long eventId) {
        return svc.calculateBalance(eventId).toPlainString();
    }

    @GetMapping(value = "/event/{eventId}/report", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> report(@PathVariable Long eventId, @RequestParam String eventName) {
        var records = svc.listByEvent(eventId);
        var balance = svc.calculateBalance(eventId);
        byte[] pdf = reports.eventFinanceReport(eventName, records, balance);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=finance_event_" + eventId + ".pdf").body(pdf);
    }

    @GetMapping("/event/{eventId}/summary")
    public Map<String, Object> summary(@PathVariable Long eventId) {
        Map<String, Object> out = new HashMap<>();
        out.put("balance", svc.calculateBalance(eventId));
        var byCat = svc.totalsByCategory(eventId);
        Map<String, String> cat = new HashMap<>();
        for (var e : byCat.entrySet()) cat.put(e.getKey().name(), e.getValue().toPlainString());
        out.put("categories", cat);
        return out;
    }

    @PutMapping("/{id}/attach-upload/{uploadId}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public FinancialRecord attach(@PathVariable Long id, @PathVariable Long uploadId) {
        var rec = repo.findById(id).orElseThrow();
        rec.setUploadId(uploadId);
        return repo.save(rec);
    }

    @GetMapping(value = "/event/{eventId}/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<String> exportCsv(@PathVariable Long eventId) {
        var sb = new StringBuilder("type,amount,description,timestamp,uploadId\n");
        for (var r : svc.listByEvent(eventId)) {
            sb.append(r.getType()).append(',')
                    .append(r.getAmount()).append(',')
                    .append(r.getDescription() == null ? "" : r.getDescription().replace(",", " ")).append(',')
                    .append(r.getTimestamp()).append(',')
                    .append(r.getUploadId() == null ? "" : r.getUploadId()).append('\n');
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=finance_event_" + eventId + ".csv")
                .body(sb.toString());
    }
}
