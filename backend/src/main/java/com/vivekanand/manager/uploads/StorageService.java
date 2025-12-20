package com.vivekanand.manager.uploads;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

public interface StorageService {

    Upload store(MultipartFile file);
    Optional<String> downloadUrl(Upload upload);
    Resource loadAsResource(Upload upload);

}
