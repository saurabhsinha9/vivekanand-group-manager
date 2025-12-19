package com.vivekanand.manager.uploads;

import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/uploads")
public class PublicImageController {
    private final UploadRepository repo;
    public PublicImageController(UploadRepository r){ this.repo = r; }

    @GetMapping("/{id}")
    public ResponseEntity<FileSystemResource> image(@PathVariable Long id) {
        var u = repo.findById(id).orElseThrow();
        String ct = (u.getContentType() == null ? "" : u.getContentType()).toLowerCase();
        // Only allow images publicly
        if (!(ct.startsWith("image/") || ct.equals("video/mp4"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(null);
        }
        var fs = new FileSystemResource(u.getStoragePath());
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(u.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + u.getOriginalFilename())
                .body(fs);
    }
}
