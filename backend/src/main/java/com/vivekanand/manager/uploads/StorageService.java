
package com.vivekanand.manager.uploads;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;

@Service
public class StorageService {
    private final Path baseDir;
    private final long maxBytes;
    private final Set<String> allowed = Set.of("application/pdf", "image/png", "image/jpeg", "image/jpg", "video/mp4");

    public StorageService(@Value("${storage.uploadDir}") String dir, @Value("${storage.maxFileSizeMb}") int maxMb) {
        baseDir = Paths.get(dir);
        maxBytes = maxMb * 1024L * 1024L;
    }

    public Upload store(MultipartFile file, UploadRepository repo) {
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("File too large");
        if (!allowed.contains(file.getContentType())) throw new IllegalArgumentException("Unsupported file type");
        String fn = StringUtils.cleanPath(file.getOriginalFilename());
        var d = LocalDate.now();
        Path p = baseDir.resolve(d.getYear() + "/" + d.getMonthValue());
        try {
            Files.createDirectories(p);
            Path target = p.resolve(System.currentTimeMillis() + "_" + fn);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            Upload u = new Upload();
            u.setOriginalFilename(fn);
            u.setContentType(file.getContentType());
            u.setSizeBytes(file.getSize());
            u.setStoragePath(target.toString());
            return repo.save(u);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed", e);
        }
    }
}
