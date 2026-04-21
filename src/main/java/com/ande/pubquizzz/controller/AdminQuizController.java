package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.dto.QuizDocumentDTO;
import com.ande.pubquizzz.dto.UpdateQuizDatesRequest;
import com.ande.pubquizzz.service.DocumentStorageService;
import com.ande.pubquizzz.service.ImageStorageService;
import com.ande.pubquizzz.service.QuizService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
public class AdminQuizController {

    private final QuizService quizService;
    private final ImageStorageService imageStorageService;
    private final DocumentStorageService documentStorageService;

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        log.info("GET /admin/quizzes");
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @PostMapping(value = "/create-quiz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createQuiz(
            @RequestPart("quiz") @Valid CreateQuizRequest request,
            @RequestParam Map<String, MultipartFile> allFiles) {
        log.info("POST /admin/create-quiz - questions={}, uploadedParts={}", request.getQuestions().size(), allFiles.size());
        injectImageUrls(request, allFiles);
        quizService.createQuiz(request);
        return ResponseEntity.ok("Quiz created successfully");
    }

    @GetMapping("/quiz/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        log.info("GET /admin/quiz/{}", id);
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{id}/detail")
    public ResponseEntity<QuizDetailDTO> getQuizDetailById(@PathVariable Long id) {
        log.info("GET /admin/quiz/{}/detail", id);
        return quizService.getQuizDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        log.info("DELETE /admin/quiz/{}", id);
        if (!quizService.deleteQuiz(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Quiz deleted successfully");
    }

    @PutMapping(value = "/quiz/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateQuizFull(
            @PathVariable Long id,
            @RequestPart("quiz") @Valid CreateQuizRequest request,
            @RequestParam Map<String, MultipartFile> allFiles) {
        log.info("PUT /admin/quiz/{} - questions={}, uploadedParts={}", id, request.getQuestions().size(), allFiles.size());
        injectImageUrls(request, allFiles);
        quizService.updateQuizFull(id, request);
        return ResponseEntity.ok("Quiz updated successfully");
    }

    @PatchMapping("/quiz/{id}/dates")
    public ResponseEntity<String> updateQuizDates(@PathVariable Long id, @RequestBody @Valid UpdateQuizDatesRequest request) {
        log.info("PATCH /admin/quiz/{}/dates - pubDate={}, submitDate={}", id, request.getPubDate(), request.getSubmitDate());
        quizService.updateQuiz(id, request.getPubDate(), request.getSubmitDate());
        return ResponseEntity.ok("Quiz updated successfully");
    }

    @DeleteMapping("/cleanup-images")
    public ResponseEntity<CleanupResult> cleanupOrphanedImages() {
        log.info("DELETE /admin/cleanup-images");
        return ResponseEntity.ok(quizService.cleanupOrphanedImages());
    }

    // ── Document endpoints ────────────────────────────────────────────────────

    @PostMapping(value = "/quiz/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<QuizDocumentDTO> uploadDocument(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        log.info("POST /admin/quiz/{}/documents - fileName={}, size={}", id, file.getOriginalFilename(), file.getSize());
        QuizDocumentDTO dto = documentStorageService.storeDocument(id, file);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/quiz/{id}/documents")
    public ResponseEntity<List<QuizDocumentDTO>> listDocuments(@PathVariable Long id) {
        log.info("GET /admin/quiz/{}/documents", id);
        return ResponseEntity.ok(documentStorageService.listDocuments(id));
    }

    @GetMapping("/quiz/{id}/documents/{docId}")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        log.info("GET /admin/quiz/{}/documents/{}", id, docId);
        DocumentStorageService.DocumentDownload download = documentStorageService.getDocumentForDownload(id, docId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(download.contentType()));
        headers.setContentDisposition(
                ContentDisposition.attachment().filename(download.originalFilename()).build());
        return ResponseEntity.ok()
                .headers(headers)
                .body(download.resource());
    }

    @DeleteMapping("/quiz/{id}/documents/{docId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable Long id,
            @PathVariable Long docId) {
        log.info("DELETE /admin/quiz/{}/documents/{}", id, docId);
        documentStorageService.deleteDocument(id, docId);
        return ResponseEntity.ok("Dokument gelöscht");
    }

    private void injectImageUrls(CreateQuizRequest request, Map<String, MultipartFile> allFiles) {
        for (CreateQuizRequest.QuestionData qd : request.getQuestions()) {
            if (qd.getNumber() >= 5 && qd.getNumber() <= 8) {
                MultipartFile answerImage = allFiles.get("answer_image_q" + qd.getNumber());
                if (answerImage != null && !answerImage.isEmpty()) {
                    qd.setAnswerImageUrl(imageStorageService.store(answerImage));
                }
            }

            for (int j = 0; j < qd.getHints().size(); j++) {
                int q = qd.getNumber();
                int h = j + 1;
                CreateQuizRequest.HintData hint = qd.getHints().get(j);

                MultipartFile atStart = allFiles.get("hint_atstart_q" + q + "_h" + h);
                if (atStart != null && !atStart.isEmpty()) {
                    hint.setImageUrlAtStart(imageStorageService.store(atStart));
                }

                MultipartFile asHint = allFiles.get("hint_ashint_q" + q + "_h" + h);
                if (asHint != null && !asHint.isEmpty()) {
                    hint.setImageUrlAsHint(imageStorageService.store(asHint));
                }
            }
        }
    }
}
