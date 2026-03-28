package com.ande.pubquizzz;

import com.ande.pubquizzz.service.ImageStorageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ImageStorageServiceTest {

    @TempDir
    Path tempDir;

    private ImageStorageService service() throws IOException {
        return new ImageStorageService(tempDir.toString());
    }

    @Test
    void rejectsNonImageContentType() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "doc.pdf", "application/pdf", "data".getBytes());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> svc.store(file));
        assertTrue(ex.getMessage().contains("application/pdf"));
    }

    @Test
    void rejectsNullContentType() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "img.png", null, "data".getBytes());

        assertThrows(IllegalArgumentException.class, () -> svc.store(file));
    }

    @Test
    void storesImageAndReturnsUploadsUrl() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "photo.jpg", "image/jpeg", "imgdata".getBytes());

        String url = svc.store(file);

        assertTrue(url.startsWith("/uploads/"), "URL should start with /uploads/");
        assertTrue(url.endsWith(".jpg"), "URL should preserve .jpg extension");

        // File should actually exist on disk
        String filename = url.substring("/uploads/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)), "Stored file should exist on disk");
    }

    @Test
    void preservesExtensionForPng() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "pngdata".getBytes());

        String url = svc.store(file);

        assertTrue(url.endsWith(".png"));
    }

    @Test
    void handlesFileWithNoExtension() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "imagewithnoext", "image/jpeg", "data".getBytes());

        String url = svc.store(file);

        assertTrue(url.startsWith("/uploads/"));
        // No extension — filename is just a UUID
        assertFalse(url.contains("."), "No dot expected when original file has no extension");
    }

    @Test
    void eachStoredFileGetsUniqueUrl() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "img.png", "image/png", "data".getBytes());

        String url1 = svc.store(file);
        String url2 = svc.store(file);

        assertNotEquals(url1, url2, "Each upload should produce a unique URL");
    }

    @Test
    void delete_removesFileFromDisk() throws IOException {
        ImageStorageService svc = service();
        MockMultipartFile file = new MockMultipartFile("file", "del.jpg", "image/jpeg", "data".getBytes());
        String url = svc.store(file);

        String filename = url.substring("/uploads/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)), "File should exist before delete");

        svc.delete(url);

        assertFalse(Files.exists(tempDir.resolve(filename)), "File should be gone after delete");
    }

    @Test
    void delete_withNullUrl_doesNothing() throws IOException {
        ImageStorageService svc = service();
        // Should not throw
        assertDoesNotThrow(() -> svc.delete(null));
    }

    @Test
    void delete_withMissingFile_doesNotThrow() throws IOException {
        ImageStorageService svc = service();
        // File was never stored — should silently succeed
        assertDoesNotThrow(() -> svc.delete("/uploads/nonexistent-file.jpg"));
    }
}
