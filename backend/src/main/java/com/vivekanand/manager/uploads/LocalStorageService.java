
package com.vivekanand.manager.uploads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path baseDir;
    private final long maxBytes;
    private final Set<String> allowed = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/jpg", "video/mp4"
    );
    private final UploadRepository repo;

    public LocalStorageService(
            UploadRepository repo,
            @Value("${storage.uploadDir}") String dir,
            @Value("${storage.maxFileSizeMb}") int maxMb) {
        this.repo = repo;
        this.baseDir = Paths.get(dir);
        this.maxBytes = maxMb * 1024L * 1024L;
    }

    @Override
    public Upload store(MultipartFile file) {
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("File too large");
        if (!allowed.contains(file.getContentType())) throw new IllegalArgumentException("Unsupported file type");

        String fn = StringUtils.cleanPath(file.getOriginalFilename());
        LocalDate d = LocalDate.now();
        Path p = baseDir.resolve(d.getYear() + "/" + String.format("%02d", d.getMonthValue())).normalize();

        try {
            if (!p.startsWith(baseDir)) {
                throw new IllegalArgumentException("Invalid storage path");
            }
            Files.createDirectories(p);
            Path target = p.resolve(System.currentTimeMillis() + "_" + fn).normalize();
            try (InputStream in = file.getInputStream()) {
                Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
            }

            Upload u = new Upload();
            u.setOriginalFilename(fn);
            u.setContentType(file.getContentType());
            u.setSizeBytes(file.getSize());
            u.setStoragePath(target.toString());
            u.setUploadedAt(Instant.now());

            return repo.save(u);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }

    }

    @Override
    public java.util.Optional<String> downloadUrl(Upload upload) {
        // local files are not served via remote URL
        return java.util.Optional.empty();
    }

    @Override
    public Resource loadAsResource(Upload upload) {
        // local: storagePath is a filesystem path
        return new FileSystemResource(upload.getStoragePath());
    }

    @Override
    public void delete(Upload upload) {
        try {
            Path path = Paths.get(upload.getStoragePath());
            Files.deleteIfExists(path);
            repo.delete(upload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete local file", e);
        }
    }
}

