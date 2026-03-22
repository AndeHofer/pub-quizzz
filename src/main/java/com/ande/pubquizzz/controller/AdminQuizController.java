package com.ande.pubquizzz.controller;

import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.service.ImageStorageService;
import com.ande.pubquizzz.service.QuizService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
public class AdminQuizController {

    private final QuizService quizService;
    private final ImageStorageService imageStorageService;

    @GetMapping("/quizzes")
    public ResponseEntity<List<QuizDTO>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @PostMapping(value = "/create-quiz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createQuiz(
            @RequestPart("quiz") CreateQuizRequest request,
            @RequestParam Map<String, MultipartFile> allFiles) {
        injectImageUrls(request, allFiles);
        quizService.createQuiz(request);
        return ResponseEntity.ok("Quiz created successfully");
    }

    @GetMapping("/quiz/{id}")
    public ResponseEntity<QuizDTO> getQuizById(@PathVariable Long id) {
        return quizService.getQuizById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{id}/detail")
    public ResponseEntity<QuizDetailDTO> getQuizDetailById(@PathVariable Long id) {
        return quizService.getQuizDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/quiz/{id}")
    public ResponseEntity<String> deleteQuiz(@PathVariable Long id) {
        if (!quizService.deleteQuiz(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok("Quiz deleted successfully");
    }

    @PutMapping(value = "/quiz/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateQuizFull(
            @PathVariable Long id,
            @RequestPart("quiz") CreateQuizRequest request,
            @RequestParam Map<String, MultipartFile> allFiles) {
        injectImageUrls(request, allFiles);
        quizService.updateQuizFull(id, request);
        return ResponseEntity.ok("Quiz updated successfully");
    }

    @PutMapping("/quiz/{id}/dates")
    public ResponseEntity<String> updateQuizDates(@PathVariable Long id, @RequestBody QuizDTO updatedQuiz) {
        return quizService.updateQuiz(id, updatedQuiz.getPubDate(), updatedQuiz.getSubmitDate())
                .map(q -> ResponseEntity.ok("Quiz updated successfully"))
                .orElse(ResponseEntity.notFound().build());
    }

    private void injectImageUrls(CreateQuizRequest request, Map<String, MultipartFile> allFiles) {
        for (CreateQuizRequest.QuestionData qd : request.getQuestions()) {
            for (int j = 0; j < qd.getHints().size(); j++) {
                String partName = "hint_image_q" + qd.getNumber() + "_h" + (j + 1);
                MultipartFile file = allFiles.get(partName);
                if (file != null && !file.isEmpty()) {
                    String url = imageStorageService.store(file);
                    qd.getHints().get(j).setImageUrl(url);
                }
            }
        }
    }
}
