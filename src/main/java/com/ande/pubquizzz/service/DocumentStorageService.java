package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.QuizDocument;
import com.ande.pubquizzz.database.repositories.QuizDocumentRepository;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.dto.QuizDocumentDTO;
import com.ande.pubquizzz.exception.ImageStorageException;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DocumentStorageService {

    private final Path documentDir;
    private final QuizDocumentRepository documentRepository;
    private final QuizRepository quizRepository;

    public DocumentStorageService(
            @Value("${app.upload.dir:/data/uploads}") String uploadDirPath,
            QuizDocumentRepository documentRepository,
            QuizRepository quizRepository) throws IOException {
        this.documentDir = Paths.get(uploadDirPath, "documents");
        Files.createDirectories(this.documentDir);
        this.documentRepository = documentRepository;
        this.quizRepository = quizRepository;
        log.debug("Document directory: {}", this.documentDir.toAbsolutePath());
    }

    @Transactional
    public QuizDocumentDTO storeDocument(Long quizId, MultipartFile file) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz nicht gefunden: " + quizId));

        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String storedFilename = UUID.randomUUID() + extension;
        Path target = documentDir.resolve(storedFilename);

        try {
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            log.error("Error sorting file: {}", e.getMessage());
            throw new ImageStorageException("Fehler beim Speichern der Datei", e);
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        QuizDocument doc = new QuizDocument();
        doc.setQuiz(quiz);
        doc.setOriginalFilename(originalFilename);
        doc.setStoredFilename(storedFilename);
        doc.setContentType(contentType);
        doc.setFileSize(file.getSize());
        doc.setUploadedAt(LocalDateTime.now());

        documentRepository.save(doc);
        log.info("Stored document {} for quiz {}", storedFilename, quizId);

        return toDTO(doc);
    }

    @Transactional(readOnly = true)
    public List<QuizDocumentDTO> listDocuments(Long quizId) {
        if (!quizRepository.existsById(quizId)) {
            log.error("Quiz not found for listing documents: {}", quizId);
            throw new ResourceNotFoundException("Quiz nicht gefunden: " + quizId);
        }
        return documentRepository.findByQuiz_QuizId(quizId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public DocumentDownload getDocumentForDownload(Long quizId, Long docId) {
        QuizDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden: " + docId));
        if (doc.getQuiz().getQuizId() != quizId) {
            throw new ResourceNotFoundException("Dokument nicht gefunden: " + docId);
        }

        Path filePath = documentDir.resolve(doc.getStoredFilename());
        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Datei nicht lesbar: " + doc.getStoredFilename());
            }
            return new DocumentDownload(resource, doc.getOriginalFilename(), doc.getContentType());
        } catch (MalformedURLException e) {
            throw new ImageStorageException("Fehler beim Lesen der Datei", e);
        }
    }

    @Transactional
    public void deleteDocument(Long quizId, Long docId) {
        QuizDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new ResourceNotFoundException("Dokument nicht gefunden: " + docId));
        if (doc.getQuiz().getQuizId() != quizId) {
            throw new ResourceNotFoundException("Dokument nicht gefunden: " + docId);
        }
        deleteFileFromDisk(doc.getStoredFilename());
        documentRepository.delete(doc);
        log.info("Deleted document {} for quiz {}", docId, quizId);
    }

    /**
     * Deletes all documents for a given quiz (called when quiz is deleted).
     * Does not throw if a file is missing on disk.
     */
    @Transactional
    public void deleteAllDocumentsForQuiz(Long quizId) {
        List<QuizDocument> docs = documentRepository.findByQuiz_QuizId(quizId);
        docs.forEach(doc -> deleteFileFromDisk(doc.getStoredFilename()));
        documentRepository.deleteByQuiz_QuizId(quizId);
        log.info("Deleted {} document(s) for quiz {}", docs.size(), quizId);
    }

    private void deleteFileFromDisk(String storedFilename) {
        Path target = documentDir.resolve(storedFilename);
        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.warn("Could not delete document file {}: {}", target, e.getMessage());
        }
    }

    private QuizDocumentDTO toDTO(QuizDocument doc) {
        return new QuizDocumentDTO(
                doc.getId(),
                doc.getQuiz().getQuizId(),
                doc.getOriginalFilename(),
                doc.getContentType(),
                doc.getFileSize(),
                doc.getUploadedAt()
        );
    }

    /**
     * Value object for a document download.
     */
    public record DocumentDownload(Resource resource, String originalFilename, String contentType) {
    }
}
