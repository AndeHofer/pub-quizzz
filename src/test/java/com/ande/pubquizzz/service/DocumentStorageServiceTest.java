package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.entities.QuizDocument;
import com.ande.pubquizzz.database.repositories.QuizDocumentRepository;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.dto.QuizDocumentDTO;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentStorageServiceTest {

    @TempDir
    Path tempDir;

    @Mock
    private QuizDocumentRepository documentRepository;

    @Mock
    private QuizRepository quizRepository;

    private DocumentStorageService service() throws IOException {
        return new DocumentStorageService(tempDir.toString(), documentRepository, quizRepository);
    }

    private Quiz quiz(long id) {
        Quiz q = new Quiz();
        q.setQuizId(id);
        q.setPubDate(LocalDate.now());
        q.setSubmitDate(LocalDate.now());
        return q;
    }

    private QuizDocument savedDoc(Quiz quiz, String storedFilename, String original) {
        QuizDocument doc = new QuizDocument();
        doc.setId(1L);
        doc.setQuiz(quiz);
        doc.setOriginalFilename(original);
        doc.setStoredFilename(storedFilename);
        doc.setContentType("application/pdf");
        doc.setFileSize(42L);
        doc.setUploadedAt(LocalDateTime.now());
        return doc;
    }

    @Test
    void storeDocument_savesFileToDisk() throws IOException {
        DocumentStorageService svc = service();
        Quiz quiz = quiz(1L);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(quiz));
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "pdfdata".getBytes());

        ArgumentCaptor<QuizDocument> captor = ArgumentCaptor.forClass(QuizDocument.class);
        when(documentRepository.save(any())).thenAnswer(inv -> {
            QuizDocument d = inv.getArgument(0);
            d.setId(1L);
            return d;
        });

        QuizDocumentDTO dto = svc.storeDocument(1L, file);

        verify(documentRepository).save(captor.capture());
        QuizDocument saved = captor.getValue();
        assertEquals("test.pdf", saved.getOriginalFilename());
        assertTrue(saved.getStoredFilename().endsWith(".pdf"));
        Path storedFile = tempDir.resolve("documents").resolve(saved.getStoredFilename());
        assertTrue(Files.exists(storedFile), "File should be stored on disk");
        assertEquals("test.pdf", dto.getOriginalFilename());
    }

    @Test
    void storeDocument_quizNotFound_throwsResourceNotFoundException() throws IOException {
        DocumentStorageService svc = service();
        when(quizRepository.findById(99L)).thenReturn(Optional.empty());
        MockMultipartFile file = new MockMultipartFile("file", "x.pdf", "application/pdf", "d".getBytes());

        assertThrows(ResourceNotFoundException.class, () -> svc.storeDocument(99L, file));
    }

    @Test
    void listDocuments_quizNotFound_throwsResourceNotFoundException() throws IOException {
        DocumentStorageService svc = service();
        when(quizRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> svc.listDocuments(99L));
    }

    @Test
    void listDocuments_returnsDocuments() throws IOException {
        DocumentStorageService svc = service();
        Quiz quiz = quiz(1L);
        when(quizRepository.existsById(1L)).thenReturn(true);
        QuizDocument doc = savedDoc(quiz, "abc.pdf", "report.pdf");
        when(documentRepository.findByQuiz_QuizId(1L)).thenReturn(List.of(doc));

        List<QuizDocumentDTO> result = svc.listDocuments(1L);

        assertEquals(1, result.size());
        assertEquals("report.pdf", result.get(0).getOriginalFilename());
    }

    @Test
    void deleteDocument_removesFileAndRecord() throws IOException {
        DocumentStorageService svc = service();
        Quiz quiz = quiz(1L);
        // Create a real file to delete
        Path docDir = tempDir.resolve("documents");
        Files.createDirectories(docDir);
        Path stored = docDir.resolve("del.pdf");
        Files.writeString(stored, "content");

        QuizDocument doc = savedDoc(quiz, "del.pdf", "original.pdf");
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        svc.deleteDocument(1L, 1L);

        assertFalse(Files.exists(stored), "File should be deleted from disk");
        verify(documentRepository).delete(doc);
    }

    @Test
    void deleteDocument_notFound_throwsResourceNotFoundException() throws IOException {
        DocumentStorageService svc = service();
        when(documentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> svc.deleteDocument(1L, 99L));
    }

    @Test
    void deleteAllDocumentsForQuiz_deletesFilesAndRecords() throws IOException {
        DocumentStorageService svc = service();
        Quiz quiz = quiz(2L);
        Path docDir = tempDir.resolve("documents");
        Files.createDirectories(docDir);
        Path stored = docDir.resolve("q2doc.pdf");
        Files.writeString(stored, "data");

        QuizDocument doc = savedDoc(quiz, "q2doc.pdf", "myfile.pdf");
        when(documentRepository.findByQuiz_QuizId(2L)).thenReturn(List.of(doc));

        svc.deleteAllDocumentsForQuiz(2L);

        assertFalse(Files.exists(stored), "File should be deleted");
        verify(documentRepository).deleteByQuiz_QuizId(2L);
    }

    @Test
    void getDocumentForDownload_docBelongsToDifferentQuiz_throws() throws IOException {
        DocumentStorageService svc = service();
        Quiz quiz = quiz(1L);
        QuizDocument doc = savedDoc(quiz, "x.pdf", "x.pdf");
        // doc.quiz.quizId is 0 (default), but we ask for quizId=99
        when(documentRepository.findById(1L)).thenReturn(Optional.of(doc));

        assertThrows(ResourceNotFoundException.class, () -> svc.getDocumentForDownload(99L, 1L));
    }
}
