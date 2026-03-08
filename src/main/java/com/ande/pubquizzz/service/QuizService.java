package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.dto.QuizDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizRepository quizRepository;

    @Transactional(readOnly = true)
    public List<QuizDTO> getAllQuizzes() {
        log.info("Fetching all quizzes");
        return quizRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<QuizDTO> getQuizById(Long id) {
        log.info("Fetching quiz with ID: {}", id);
        return quizRepository.findById(id).map(this::toDTO);
    }

    @Transactional
    public QuizDTO createQuiz(CreateQuizRequest request) {
        log.info("Creating new quiz entry");
        Quiz quiz = new Quiz();
        quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());

        for (CreateQuizRequest.QuestionData questionData : request.getQuestions()) {
            quiz.addQuestion(
                    questionData.getNumber(),
                    questionData.getQuestion(),
                    questionData.getAnswer(),
                    questionData.getNote(),
                    questionData.getHints()
            );
        }

        quizRepository.save(quiz);
        log.info("Quiz saved successfully with ID: {}", quiz.getQuizId());
        return toDTO(quiz);
    }

    @Transactional
    public Optional<QuizDTO> updateQuiz(Long id, LocalDate pubDate, LocalDate submitDate) {
        log.info("Updating quiz with ID: {}", id);
        return quizRepository.findById(id).map(quiz -> {
            quiz.setPubDate(pubDate);
            quiz.setSubmitDate(submitDate);
            quizRepository.save(quiz);
            log.info("Quiz {} updated successfully", id);
            return toDTO(quiz);
        });
    }

    @Transactional
    public boolean deleteQuiz(Long id) {
        log.info("Deleting quiz with ID: {}", id);
        if (!quizRepository.existsById(id)) {
            return false;
        }
        quizRepository.deleteById(id);
        log.info("Quiz {} deleted successfully", id);
        return true;
    }

    private QuizDTO toDTO(Quiz quiz) {
        QuizDTO dto = new QuizDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setPubDate(quiz.getPubDate());
        dto.setSubmitDate(quiz.getSubmitDate());
        dto.setQuestionCount(quiz.getQuestions() != null ? quiz.getQuestions().size() : 0);
        return dto;
    }
}
