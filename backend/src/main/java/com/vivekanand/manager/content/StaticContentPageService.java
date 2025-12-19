package com.vivekanand.manager.content;

import com.vivekanand.manager.content.dto.StaticContentPageResponse;
import com.vivekanand.manager.content.dto.StaticContentPageUpdateRequest;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class StaticContentPageService {

    private final StaticContentPageRepository repo;

    public StaticContentPageService(StaticContentPageRepository repo) {
        this.repo = repo;
    }

    public StaticContentPageResponse getPublished(String slug) {
        var page = repo.findBySlug(slug).orElseThrow(() -> new RuntimeException("Page not found: " + slug));
        var content = page.getPublishedContent() != null ? page.getPublishedContent() : "";
        return new StaticContentPageResponse(page.getSlug(), page.getTitle(), content, page.getStatus().name());
    }

    public StaticContentPageResponse getDraft(String slug) {
        var page = repo.findBySlug(slug).orElseThrow(() -> new RuntimeException("Page not found: " + slug));
        var content = page.getDraftContent() != null ? page.getDraftContent() : page.getPublishedContent();
        return new StaticContentPageResponse(page.getSlug(), page.getTitle(), content, page.getStatus().name());
    }

    @Transactional
    public StaticContentPageResponse saveDraft(String slug, StaticContentPageUpdateRequest req) {
        var page = repo.findBySlug(slug).orElseGet(() -> {
            var p = new StaticContentPage();
            p.setSlug(slug);
            p.setStatus(StaticContentPage.Status.DRAFT);
            return p;
        });
        page.setTitle(req.title());
        page.setDraftContent(req.content());
        page.setUpdatedAt(Instant.now());
        page.setStatus(StaticContentPage.Status.DRAFT);
        repo.save(page);
        return new StaticContentPageResponse(page.getSlug(), page.getTitle(), page.getDraftContent(), page.getStatus().name());
    }

    @Transactional
    public StaticContentPageResponse publish(String slug) {
        var page = repo.findBySlug(slug).orElseThrow(() -> new RuntimeException("Page not found: " + slug));
        // If no explicit draft, publish whatever is available
        var toPublish = page.getDraftContent() != null ? page.getDraftContent() : page.getPublishedContent();
        page.setPublishedContent(toPublish);
        page.setPublishedAt(Instant.now());
        page.setStatus(StaticContentPage.Status.PUBLISHED);
        repo.save(page);
        return new StaticContentPageResponse(page.getSlug(), page.getTitle(), page.getPublishedContent(), page.getStatus().name());
    }
}
