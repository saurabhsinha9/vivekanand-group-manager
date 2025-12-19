
package com.vivekanand.manager.uploads;

import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {
    private final UploadRepository repo;
    private final StorageService storage;

    public UploadController(UploadRepository r, StorageService s) {
        repo = r;
        storage = s;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public Upload upload(@RequestParam("file") MultipartFile file) {
        return storage.store(file, repo);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MEMBER')")
    public ResponseEntity<FileSystemResource> download(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();
        // TODO: add ownership checks if needed (e.g., member who uploaded or linked to an event they belong to)
        FileSystemResource fs = new FileSystemResource(u.getStoragePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(u.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + u.getOriginalFilename())
                .body(fs);
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


}
