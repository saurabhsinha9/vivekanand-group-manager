package com.vivekanand.manager.uploads;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "cloudinary")
public class CloudinaryStorageService implements StorageService {

    private final Cloudinary cloudinary;
    private final UploadRepository repo;
    private final long maxBytes;
    private final Set<String> allowed = Set.of(
            "application/pdf", "image/png", "image/jpeg", "image/jpg", "video/mp4"
    );
    private final String baseFolder;

    public CloudinaryStorageService(
            Cloudinary cloudinary,
            UploadRepository repo,
            @Value("${storage.maxFileSizeMb}") int maxMb,
            @Value("${cloudinary.folder:myapp/uploads}") String baseFolder
    ) {
        this.cloudinary = cloudinary;
        this.repo = repo;
        this.maxBytes = maxMb * 1024L * 1024L;
        this.baseFolder = baseFolder;
    }

    @Override
    public Upload store(MultipartFile file) {
        if (file.getSize() > maxBytes) throw new IllegalArgumentException("File too large");
        if (!allowed.contains(file.getContentType())) throw new IllegalArgumentException("Unsupported file type");

        String fn = StringUtils.cleanPath(file.getOriginalFilename());
        LocalDate d = LocalDate.now();
        String folderPath = baseFolder + "/" + d.getYear() + "/" + String.format("%02d", d.getMonthValue());

        try {
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "auto",       // image/video/raw(pdf)
                    "folder", folderPath,
                    "use_filename", true,
                    "unique_filename", true
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);
            String secureUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");
            Object bytesObj = uploadResult.getOrDefault("bytes", file.getSize());
            long bytes = (bytesObj instanceof Number) ? ((Number) bytesObj).longValue() : file.getSize();

            Upload u = new Upload();
            u.setOriginalFilename(fn);
            u.setContentType(file.getContentType()); // client-reported
            u.setSizeBytes(bytes);
            u.setProviderId(publicId);
            u.setStoragePath(secureUrl);
            u.setUploadedAt(Instant.now());
            return repo.save(u);
        } catch (Exception e) {
            throw new RuntimeException("Cloudinary upload failed", e);
        }
    }

    @Override
    public Optional<String> downloadUrl(Upload upload) {
        // For Cloudinary, storagePath holds secure URL
        String path = upload.getStoragePath();
        if (path != null && (path.startsWith("http://") || path.startsWith("https://"))) {
            return Optional.of(path);
        }
        return Optional.empty();
    }

    @Override
    public Resource loadAsResource(Upload upload) {
        throw new UnsupportedOperationException("Cloudinary resources must be accessed via URL");
    }

    @Override
    public void delete(Upload upload) {
        try {
            if (upload.getProviderId() != null) {
                cloudinary.uploader().destroy(upload.getProviderId(), ObjectUtils.emptyMap());
            }
            repo.delete(upload);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete Cloudinary file", e);
        }
    }
}
