package com.ande.pubquizzz.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.exception.ImageStorageException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class ImageStorageService {

    private final Path uploadDir;

    public ImageStorageService(@Value("${app.upload.dir:/data/uploads}") String uploadDirPath) throws IOException {
        this.uploadDir = Paths.get(uploadDirPath);
        Files.createDirectories(this.uploadDir);
        log.debug("Upload directory: {}", this.uploadDir.toAbsolutePath());
    }

    /**
     * Stores an uploaded image file and returns its public URL path (e.g. "/uploads/abc123.jpg").
     * Throws IllegalArgumentException if the file is not an image.
     */
    public String store(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            log.error("Invalid image file: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("Only image files are accepted. Got: " + contentType);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String filename = UUID.randomUUID() + extension;
        Path target = uploadDir.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new ImageStorageException("Failed to store image file", e);
        }
        log.info("Stored image: {}", target);

        return "/uploads/" + filename;
    }

    /**
     * Deletes a previously stored image by its public URL (e.g. "/uploads/abc123.jpg").
     * No-op if url is null or the file does not exist.
     */
    public void delete(String url) {
        if (url == null) {
            return;
        }
        String prefix = "/uploads/";
        if (!url.startsWith(prefix)) {
            log.warn("Unrecognized image URL format, skipping delete: {}", url);
            return;
        }
        String filename = url.substring(prefix.length());
        Path target = uploadDir.resolve(filename);
        try {
            Files.deleteIfExists(target);
            log.info("Deleted image: {}", target);
        } catch (IOException e) {
            log.warn("Could not delete image file {}: {}", target, e.getMessage());
        }
    }

    /**
     * Deletes every file in the upload directory whose URL is not in {@code referencedUrls}.
     * Returns a {@link CleanupResult} with the count and names of deleted files.
     */
    public CleanupResult cleanupOrphanedImages(Set<String> referencedUrls) {
        List<String> deleted = new ArrayList<>();
        try (var stream = Files.list(uploadDir)) {
            stream.filter(Files::isRegularFile).forEach(file -> {
                String url = "/uploads/" + file.getFileName().toString();
                if (!referencedUrls.contains(url)) {
                    try {
                        Files.delete(file);
                        deleted.add(file.getFileName().toString());
                        log.info("Cleanup: deleted orphaned image {}", file);
                    } catch (IOException e) {
                        log.warn("Cleanup: could not delete {}: {}", file, e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            throw new ImageStorageException("Failed to list upload directory for cleanup", e);
        }
        return new CleanupResult(deleted.size(), deleted);
    }
}
