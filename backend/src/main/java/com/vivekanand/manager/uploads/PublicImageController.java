package com.vivekanand.manager.uploads;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/uploads")
public class PublicImageController {
    private final UploadRepository repo;
    @Autowired(required = false)
    private Cloudinary cloudinary; // Optional; used to generate video poster URLs
    public PublicImageController(UploadRepository r){ this.repo = r; }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> image(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();

        if (!isAllowedForPublic(u)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        final String path = u.getStoragePath();
        final String ct   = normalizeContentType(u.getContentType(), u.getOriginalFilename(), path);

        // Remote (Cloudinary/CDN): redirect for performance & range support
        if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
            if (!isAllowedRemoteHost(path)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(path))
                    .header("X-Content-Type-Options", "nosniff")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                    .build();
        }

        // Local file
        if (path == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Path p = Paths.get(path).normalize();
        if (!Files.exists(p)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        FileSystemResource fs = new FileSystemResource(p.toFile());

        return ResponseEntity.ok()
                .contentType(safeMediaType(ct))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionInline(u.getOriginalFilename()))
                .header("X-Content-Type-Options", "nosniff")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(fs);
    }

    /**
     * Poster/thumbnail endpoint:
     * - For images: returns/redirects the image itself (safe to use for previews).
     * - For Cloudinary videos: redirects to a JPG poster (frame at ~1s).
     * - For local videos: poster not available → 404.
     */
    @GetMapping("/{id}/poster")
    public ResponseEntity<?> poster(@PathVariable Long id) {
        Upload u = repo.findById(id).orElseThrow();

        final String path = u.getStoragePath();
        final String ct   = Optional.ofNullable(u.getContentType()).orElse("").toLowerCase();
        final boolean isImage = ct.startsWith("image/") || hasImageExt(u.getOriginalFilename()) || hasImageExt(path);
        final boolean isVideoMp4 = ct.equals("video/mp4") || endsWith(path, ".mp4") || endsWith(u.getOriginalFilename(), ".mp4");

        // Images: redirect/serve the original image as poster
        if (isImage) {
            if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
                if (!isAllowedRemoteHost(path)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(path))
                        .header("X-Content-Type-Options", "nosniff")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                        .build();
            }
            if (path == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            Path p = Paths.get(path).normalize();
            if (!Files.exists(p)) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            FileSystemResource fs = new FileSystemResource(p.toFile());
            String normalizedCt = normalizeContentType(ct, u.getOriginalFilename(), path);
            return ResponseEntity.ok()
                    .contentType(safeMediaType(normalizedCt))
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDispositionInline(u.getOriginalFilename()))
                    .header("X-Content-Type-Options", "nosniff")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(fs);
        }

        // Videos (mp4): try to generate Cloudinary poster (jpg)
        if (isVideoMp4) {
            String publicId = u.getProviderId();
            if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
                // Cloudinary: generate a transformation URL for a JPG poster from frame ~1s
                if (!isAllowedRemoteHost(path)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
                if (cloudinary != null && publicId != null && !publicId.isBlank()) {
                    String posterUrl = cloudinary.url()
                            .resourceType("video")
                            .secure(true)
                            .transformation(
                                    new com.cloudinary.Transformation().rawTransformation("so_1") // frame ~1s
                            )
                            .format("jpg")
                            .generate(publicId);
                    return ResponseEntity.status(HttpStatus.FOUND)
                            .location(URI.create(posterUrl))
                            .header("X-Content-Type-Options", "nosniff")
                            .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                            .build();
                }
                // If we can't build via SDK, best-effort fallback: redirect to the original video (browser may show default poster)
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(path))
                        .header("X-Content-Type-Options", "nosniff")
                        .header(HttpHeaders.CACHE_CONTROL, "public, max-age=300")
                        .build();
            } else {
                // Local video: poster generation not implemented → 404
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        // Other types: forbid posters
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    /** Accept images and mp4 videos; fall back to extension if contentType is missing */
    private boolean isAllowedForPublic(Upload u) {
        String ct = Optional.ofNullable(u.getContentType()).orElse("").toLowerCase();
        if (ct.startsWith("image/") || ct.equals("video/mp4")) return true;

        String name = Optional.ofNullable(u.getOriginalFilename()).orElse("").toLowerCase();
        String path = Optional.ofNullable(u.getStoragePath()).orElse("").toLowerCase();

        if (hasImageExt(name) || hasImageExt(path)) return true;
        if (endsWith(name, ".mp4") || endsWith(path, ".mp4")) return true;

        return false;
    }

    /** Normalize content type using filename/path if missing */
    private String normalizeContentType(String ct, String name, String path) {
        String lower = Optional.ofNullable(ct).orElse("").toLowerCase();
        if (!lower.isBlank()) return lower;

        String target = (Optional.ofNullable(name).orElse("") + " " + Optional.ofNullable(path).orElse("")).toLowerCase();
        if (target.endsWith(".png")) return "image/png";
        if (target.endsWith(".webp")) return "image/webp";
        if (target.endsWith(".jpg") || target.endsWith(".jpeg")) return "image/jpeg";
        if (target.endsWith(".mp4")) return "video/mp4";

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private boolean hasImageExt(String s) {
        return endsWith(s, ".jpg") || endsWith(s, ".jpeg") || endsWith(s, ".png") || endsWith(s, ".webp");
    }

    private boolean endsWith(String s, String suffix) {
        return Optional.ofNullable(s).orElse("").toLowerCase().endsWith(suffix);
    }

    private MediaType safeMediaType(String ct) {
        try {
            return MediaType.parseMediaType(ct != null && !ct.isBlank() ? ct : MediaType.APPLICATION_OCTET_STREAM_VALUE);
        } catch (Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String contentDispositionInline(String originalFilename) {
        String fallback = "file";
        String safe = Optional.ofNullable(originalFilename)
                .map(this::sanitizeFilename)
                .orElse(fallback);
        return "inline; filename=\"" + safe + "\"";
    }

    private String sanitizeFilename(String name) {
        return name.replaceAll("[\\p{Cntrl}\"]", "_");
    }

    /** Basic SSRF mitigation—restrict remote hosts to Cloudinary (adjust if needed) */
    private boolean isAllowedRemoteHost(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            return host != null && host.endsWith(".cloudinary.com");
        } catch (Exception e) {
            return false;
        }
    }
}
