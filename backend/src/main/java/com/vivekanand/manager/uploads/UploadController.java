
package com.vivekanand.manager.uploads;

import com.vivekanand.manager.finance.FinancialRecordRepository;
import com.vivekanand.manager.gallery.AlbumRepository;
import com.vivekanand.manager.gallery.GalleryItemRepository;
import com.vivekanand.manager.posts.MediaAttachmentRepository;
import jakarta.transaction.Transactional;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadRepository repo;
    private final StorageService storage;
    private final MediaAttachmentRepository mediaRepo;
    private final FinancialRecordRepository finRecRepo;
    private final GalleryItemRepository galItemRepo;
    private final AlbumRepository albRepo;

    public UploadController(UploadRepository repo, StorageService storage, MediaAttachmentRepository mediaRepo, FinancialRecordRepository finRecRepo, GalleryItemRepository galItemRepo, AlbumRepository albRepo) {
        this.repo = repo;
        this.storage = storage;
        this.mediaRepo = mediaRepo;
        this.finRecRepo = finRecRepo;
        this.galItemRepo = galItemRepo;
        this.albRepo = albRepo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public Upload upload(@RequestParam("file") MultipartFile file) {
        return storage.store(file);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<?> download(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();
        var maybeUrl = storage.downloadUrl(u);
        if (maybeUrl.isPresent()) {
            try {
                InputStream in = new URL(maybeUrl.get()).openStream();
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(u.getContentType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + u.getOriginalFilename() + "\"")
                        .header("X-Content-Type-Options", "nosniff")
                        .body(new InputStreamResource(in));
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Failed to fetch remote resource");
            }
        }

        Resource res = storage.loadAsResource(u);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(u.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + u.getOriginalFilename() + "\"")
                .header("X-Content-Type-Options", "nosniff")
                .body(res);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public java.util.List<Upload> list() { return repo.findAll(); }


    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public Page<Upload> page(@RequestParam(defaultValue="0") int page,
                             @RequestParam(defaultValue="20") int size) {
        return repo.findAll(PageRequest.of(page, size, Sort.by("uploadedAt").descending()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    @Transactional
    public ResponseEntity<?> delete(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();
        mediaRepo.deleteByUploadId(id);
        finRecRepo.deleteByUploadId(id);
        galItemRepo.deleteByUploadId(id);
        albRepo.deleteByCoverUploadId(id);
        storage.delete(u);
        return ResponseEntity.noContent().build();
    }


}
