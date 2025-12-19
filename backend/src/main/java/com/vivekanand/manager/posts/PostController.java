
package com.vivekanand.manager.posts;

import com.vivekanand.manager.audit.Auditable;
import com.vivekanand.manager.notifications.NotificationService;
import com.vivekanand.manager.posts.dto.CreatePostRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostRepository repo;
    private final MediaAttachmentRepository mediaRepo;
    private final NotificationService notifier;

    public PostController(PostRepository r, MediaAttachmentRepository m, NotificationService n) {
        repo = r;
        mediaRepo = m;
        notifier = n;
    }

    @GetMapping
    public List<Post> list() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public Post get(@PathVariable Long id) {
        return repo.findById(id).orElseThrow();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Auditable(action = "CREATE_POST", entity = "Post")
    public Post create(@Valid @RequestBody CreatePostRequest req) {
        Post p = new Post();
        p.setAuthorUserId(req.authorUserId());
        p.setMessage(applyTemplateIfNeeded(req.message(), req.templateCode()));
        p.setBroadcast(Boolean.TRUE.equals(req.broadcast()));
        var saved = repo.save(p);
        if (saved.isBroadcast()) notifier.broadcast(saved.getMessage());
        return saved;

    }

    @PostMapping("/{id}/media")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Auditable(action = "ADD_MEDIA", entity = "MediaAttachment")
    public MediaAttachment addMedia(@PathVariable Long id, @RequestBody MediaAttachment m) {
        m.setPostId(id);
        return mediaRepo.save(m);
    }


    private String applyTemplateIfNeeded(String msg, String code) {
        if (code == null || code.isBlank()) return msg;
        // simple inline templates; later move to DB
        return switch (code) {
            case "EVENT_REMINDER" -> "Reminder: " + msg;
            case "THANK_YOU" -> "Thank you! " + msg;
            default -> msg;
        };
    }

}
