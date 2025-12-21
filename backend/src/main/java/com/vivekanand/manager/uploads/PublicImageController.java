package com.vivekanand.manager.uploads;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/uploads")
public class PublicImageController {
    private final UploadRepository repo;
    public PublicImageController(UploadRepository r){ this.repo = r; }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> image(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();
        String ct = Optional.ofNullable(u.getContentType()).orElse("").toLowerCase();

        // Allow images and mp4 videos for public display
        boolean allowed = ct.startsWith("image/") || ct.equals("video/mp4");
        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            if (u.getStoragePath() != null &&
                    (u.getStoragePath().startsWith("http://") || u.getStoragePath().startsWith("https://"))) {
                // Cloudinary: proxy stream to avoid redirect issues in <img>/<video>
                InputStream in = new URL(u.getStoragePath()).openStream();
                return ResponseEntity.ok()
                        .contentType(safeMediaType(ct))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + u.getOriginalFilename())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(new InputStreamResource(in));
            } else {
                // Local file
                var fs = new FileSystemResource(u.getStoragePath());
                return ResponseEntity.ok()
                        .contentType(safeMediaType(ct))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + u.getOriginalFilename())
                        .header("X-Content-Type-Options", "nosniff")
                        .body(fs);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private MediaType safeMediaType(String ct) {
        try {
            return MediaType.parseMediaType(ct != null && !ct.isBlank()
                    ? ct
                    : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
