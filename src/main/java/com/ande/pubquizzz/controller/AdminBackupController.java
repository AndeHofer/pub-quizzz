package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.service.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.time.LocalDate;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/admin/backup")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminBackupController {

    private final BackupService backupService;

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> export() {
        String filename = "pubquizzz-backup-" + LocalDate.now() + ".zip";
        StreamingResponseBody body = outputStream -> {
            try (ZipOutputStream zip = new ZipOutputStream(outputStream)) {
                backupService.createBackup(zip);
            }
        };
        log.info("GET /admin/backup/export - Backup export started");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importBackup(@RequestParam("file") MultipartFile file) throws IOException {
        backupService.stageRestore(file.getBytes());
        log.info("POST /admin/backup/import - Backup import staged, file size: {} bytes", file.getSize());
        return ResponseEntity.ok(
                "Backup bereit. Bitte starte die Anwendung neu, um die Wiederherstellung anzuwenden.");
    }
}
