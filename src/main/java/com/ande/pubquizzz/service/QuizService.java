package com.ande.pubquizzz.service;

import com.ande.pubquizzz.cache.InvalidateAllAppCaches;
import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.CleanupResult;
import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import com.ande.pubquizzz.exception.ResourceNotFoundException;
import com.ande.pubquizzz.mapper.QuizMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.ande.pubquizzz.config.CacheConfig.QUIZ_BY_ID;
import static com.ande.pubquizzz.config.CacheConfig.QUIZ_DETAIL_BY_ID;
import static com.ande.pubquizzz.config.CacheConfig.QUIZZES_ALL;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;
    private final ResultRepository resultRepository;
    private final QuizMapper quizMapper;
    private final ImageStorageService imageStorageService;
    private final DocumentStorageService documentStorageService;

    @Transactional(readOnly = true)
    @Cacheable(QUIZZES_ALL)
    public List<QuizDTO> getAllQuizzes() {
        log.debug("Fetching all quizzes");
        return quizRepository.findAll().stream()
                .map(quizMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = QUIZ_BY_ID, key = "#id")
    public Optional<QuizDTO> getQuizById(Long id) {
        log.debug("Fetching quiz with ID: {}", id);
        return quizRepository.findById(id).map(quizMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = QUIZ_DETAIL_BY_ID, key = "#id")
    public Optional<QuizDetailDTO> getQuizDetailById(Long id) {
        log.debug("Fetching quiz detail with ID: {}", id);
        return quizRepository.findById(id).map(quizMapper::toDetailDTO);
    }

    @Transactional
    @InvalidateAllAppCaches
    public QuizDTO createQuiz(CreateQuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());
        quiz.setCreator(request.getCreator());

        applyQuestionsToQuiz(quiz, request.getQuestions());

        quizRepository.save(quiz);
        log.info("Quiz saved successfully with ID: {}", quiz.getQuizId());
        return quizMapper.toDTO(quiz);
    }

    @Transactional
    @InvalidateAllAppCaches
    public QuizDTO updateQuiz(Long id, LocalDate pubDate, LocalDate submitDate) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz nicht gefunden: " + id));
        quiz.setPubDate(pubDate);
        quiz.setSubmitDate(submitDate);
        quizRepository.save(quiz);
        log.info("Quiz {} updated successfully", id);
        return quizMapper.toDTO(quiz);
    }

    @Transactional
    @InvalidateAllAppCaches
    public QuizDTO updateQuizFull(Long id, CreateQuizRequest request) {
        log.info("Fully updating quiz with ID: {}", id);
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz nicht gefunden: " + id));

        quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : quiz.getPubDate());
        quiz.setCreator(request.getCreator());

        // Snapshot old image URLs before clearing questions
        List<String> oldImageUrls = quiz.getQuestions().stream()
                .flatMap(q -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(q.getAnswerImageUrl()),
                        q.getHints().stream().flatMap(h -> java.util.stream.Stream.of(h.getImageUrlAtStart(), h.getImageUrlAsHint()))
                ))
                .filter(url -> url != null)
                .toList();

        // Remove all existing questions first (cascade will remove hints)
        quiz.getQuestions().clear();
        quizRepository.flush(); // Ensure deletes are done before adding new

        applyQuestionsToQuiz(quiz, request.getQuestions());

        quizRepository.save(quiz);

        // Collect URLs still in use after the update
        java.util.Set<String> newImageUrls = quiz.getQuestions().stream()
                .flatMap(q -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(q.getAnswerImageUrl()),
                        q.getHints().stream().flatMap(h -> java.util.stream.Stream.of(h.getImageUrlAtStart(), h.getImageUrlAsHint()))
                ))
                .filter(url -> url != null)
                .collect(java.util.stream.Collectors.toSet());

        // Only delete files that are no longer referenced (truly replaced or removed)
        oldImageUrls.stream()
                .filter(url -> !newImageUrls.contains(url))
                .forEach(imageStorageService::delete);

        log.info("Quiz {} fully updated successfully", id);
        return quizMapper.toDTO(quiz);
    }

    @Transactional
    @InvalidateAllAppCaches
    public boolean deleteQuiz(Long id) {
        Quiz quiz = quizRepository.findById(id).orElse(null);
        if (quiz == null) {
            return false;
        }

        // Snapshot image URLs before cascade-delete removes the hints
        List<String> imageUrls = quiz.getQuestions().stream()
                .flatMap(q -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(q.getAnswerImageUrl()),
                        q.getHints().stream().flatMap(h -> java.util.stream.Stream.of(h.getImageUrlAtStart(), h.getImageUrlAsHint()))
                ))
                .filter(url -> url != null)
                .toList();

        resultRepository.deleteByQuizQuizId(id);
        quizRepository.deleteById(id);

        imageUrls.forEach(imageStorageService::delete);
        documentStorageService.deleteAllDocumentsForQuiz(id);

        log.info("Quiz {} deleted successfully, {} image(s) removed", id, imageUrls.size());
        return true;
    }

    @Transactional(readOnly = true)
    public CleanupResult cleanupOrphanedImages() {
        log.info("Starting orphaned image cleanup");
        Set<String> referencedUrls = quizRepository.findAll().stream()
                .flatMap(q -> q.getQuestions().stream())
                .flatMap(q -> java.util.stream.Stream.concat(
                        java.util.stream.Stream.of(q.getAnswerImageUrl()),
                        q.getHints().stream().flatMap(h -> java.util.stream.Stream.of(h.getImageUrlAtStart(), h.getImageUrlAsHint()))
                ))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        CleanupResult result = imageStorageService.cleanupOrphanedImages(referencedUrls);
        log.info("Cleanup complete: {} file(s) deleted", result.getDeletedCount());
        return result;
    }

    private void applyQuestionsToQuiz(Quiz quiz, List<CreateQuizRequest.QuestionData> questions) {
        for (CreateQuizRequest.QuestionData questionData : questions) {
            List<Hint> hints = buildHints(questionData.getHints());
            quiz.addQuestion(
                    questionData.getNumber(),
                    questionData.getQuestionText(),
                    questionData.getAnswer() == null ? "" : questionData.getAnswer(),
                    questionData.getAnswerImageUrl(),
                    questionData.getNote(),
                    hints
            );
        }
    }

    private List<Hint> buildHints(List<CreateQuizRequest.HintData> hintDataList) {
        if (hintDataList == null) return List.of();
        List<Hint> hints = new ArrayList<>();
        for (CreateQuizRequest.HintData hd : hintDataList) {
            Hint h = new Hint();
            h.setHintText(hd.getHintText());
            h.setImageUrlAtStart(hd.getImageUrlAtStart());
            h.setImageUrlAsHint(hd.getImageUrlAsHint());
            hints.add(h);
        }
        return hints;
    }
}
