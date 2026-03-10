package com.ande.pubquizzz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload.dir:/data/uploads}") String uploadDirPath) throws IOException {
        this.uploadDir = Paths.get(uploadDirPath);
        Files.createDirectories(this.uploadDir);
        log.info("Upload directory: {}", this.uploadDir.toAbsolutePath());
    }

    /**
     * Stores an uploaded image file and returns its public URL path (e.g. "/uploads/abc123.jpg").
     * Throws IllegalArgumentException if the file is not an image.
     */
    public String store(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are accepted. Got: " + contentType);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        Files.copy(file.getInputStream(), target);
        log.info("Stored image: {}", target);

        return "/uploads/" + filename;
    }
}
