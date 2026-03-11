package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Question;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.dto.QuizDTO;
import com.ande.pubquizzz.dto.QuizDetailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @Transactional(readOnly = true)
    public Optional<QuizDetailDTO> getQuizDetailById(Long id) {
        log.info("Fetching quiz detail with ID: {}", id);
        return quizRepository.findById(id).map(this::toDetailDTO);
    }

    @Transactional
    public QuizDTO createQuiz(CreateQuizRequest request) {
        log.info("Creating new quiz entry");
        Quiz quiz = new Quiz();
        quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : LocalDate.now());
        quiz.setSubmitDate(LocalDate.now());

        for (CreateQuizRequest.QuestionData questionData : request.getQuestions()) {
            List<Hint> hints = questionData.getHints().stream()
                    .map(hd -> {
                        Hint h = new Hint();
                        h.setHintText(hd.getHintText());
                        h.setImageUrl(hd.getImageUrl());
                        return h;
                    })
                    .toList();
            quiz.addQuestion(
                    questionData.getNumber(),
                    questionData.getQuestionText(),
                    questionData.getAnswer(),
                    questionData.getNote(),
                    hints
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
    public QuizDTO updateQuizFull(Long id, CreateQuizRequest request) {
        log.info("Fully updating quiz with ID: {}", id);
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));

        quiz.setPubDate(request.getPubDate() != null ? request.getPubDate() : quiz.getPubDate());

        // Remove all existing questions first (cascade will remove hints)
        List<Question> existingQuestions = new ArrayList<>(quiz.getQuestions());
        for (Question q : existingQuestions) {
            quiz.getQuestions().remove(q);
        }
        quizRepository.flush(); // Ensure deletes are done before adding new

        // Add new questions
        for (CreateQuizRequest.QuestionData questionData : request.getQuestions()) {
            List<Hint> hints = questionData.getHints().stream()
                    .map(hd -> {
                        Hint h = new Hint();
                        h.setHintText(hd.getHintText());
                        h.setImageUrl(hd.getImageUrl());
                        return h;
                    })
                    .toList();
            quiz.addQuestion(
                    questionData.getNumber(),
                    questionData.getQuestionText(),
                    questionData.getAnswer(),
                    questionData.getNote(),
                    hints
            );
        }

        quizRepository.save(quiz);
        log.info("Quiz {} fully updated successfully", id);
        return toDTO(quiz);
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

    private QuizDetailDTO toDetailDTO(Quiz quiz) {
        QuizDetailDTO dto = new QuizDetailDTO();
        dto.setQuizId(quiz.getQuizId());
        dto.setPubDate(quiz.getPubDate());
        dto.setSubmitDate(quiz.getSubmitDate());
        if (quiz.getQuestions() != null) {
            List<QuizDetailDTO.QuestionDetailDTO> questions = quiz.getQuestions().stream()
                    .map(q -> {
                        QuizDetailDTO.QuestionDetailDTO qdto = new QuizDetailDTO.QuestionDetailDTO();
                        qdto.setNumber(q.getId().getQuestionNumber());
                        qdto.setQuestionText(q.getQuestionText());
                        qdto.setAnswer(q.getAnswer());
                        qdto.setNote(q.getNote());
                        if (q.getHints() != null) {
                            List<QuizDetailDTO.HintDetailDTO> hints = q.getHints().stream()
                                    .map(h -> {
                                        QuizDetailDTO.HintDetailDTO hdto = new QuizDetailDTO.HintDetailDTO();
                                        hdto.setHintText(h.getHintText());
                                        hdto.setImageUrl(h.getImageUrl());
                                        return hdto;
                                    })
                                    .toList();
                            qdto.setHints(hints);
                        }
                        return qdto;
                    })
                    .toList();
            dto.setQuestions(questions);
        }
        return dto;
    }
}
