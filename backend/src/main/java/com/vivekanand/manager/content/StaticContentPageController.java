package com.vivekanand.manager.content;
import com.vivekanand.manager.content.dto.StaticContentPageResponse;
import com.vivekanand.manager.content.dto.StaticContentPageUpdateRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class StaticContentPageController {

    private final StaticContentPageService service;

    public StaticContentPageController(StaticContentPageService service) {
        this.service = service;
    }

    @GetMapping("/public/pages/{slug}")
    public ResponseEntity<StaticContentPageResponse> getPublished(@PathVariable String slug) {
        return ResponseEntity.ok(service.getPublished(slug));
    }

    // Admin: get draft content (requires ADMIN)
    @GetMapping("/admin/pages/{slug}/draft")
    public ResponseEntity<StaticContentPageResponse> getDraft(@PathVariable String slug) {
        return ResponseEntity.ok(service.getDraft(slug));
    }

    // Admin: save draft
    @PutMapping("/admin/pages/{slug}")
    public ResponseEntity<StaticContentPageResponse> saveDraft(@PathVariable String slug, @RequestBody StaticContentPageUpdateRequest req) {
        return ResponseEntity.ok(service.saveDraft(slug, req));
    }

    // Admin: publish draft
    @PostMapping("/admin/pages/{slug}/publish")
    public ResponseEntity<StaticContentPageResponse> publish(@PathVariable String slug) {
        return ResponseEntity.ok(service.publish(slug));
    }
}