package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Quiz;
import com.ande.pubquizzz.database.repositories.QuizRepository;
import com.ande.pubquizzz.database.repositories.ResultRepository;
import com.ande.pubquizzz.dto.CreateQuizRequest;
import com.ande.pubquizzz.mapper.QuizMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizServiceUpdateTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private ResultRepository resultRepository;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private ImageStorageService imageStorageService;

    @Mock
    private DocumentStorageService documentStorageService;

    @InjectMocks
    private QuizService quizService;

    /**
     * Build a minimal 8-question quiz with one hint bearing both image URLs.
     */
    private Quiz quizWithImageUrls(String atStart, String asHint) {
        Hint h1 = new Hint();
        h1.setHintText("clue");
        h1.setImageUrlAtStart(atStart);
        h1.setImageUrlAsHint(asHint);

        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        quiz.addQuestion(1, "Q1", "A1", "", List.of(h1, hint("h2"), hint("h3"), hint("h4")));
        for (int i = 2; i <= 4; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(4));
        for (int i = 5; i <= 8; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(3));
        return quiz;
    }

    private Quiz quizWithAnswerImageUrl(String answerImageUrl) {
        Quiz quiz = new Quiz();
        quiz.setPubDate(LocalDate.now());
        quiz.addQuestion(1, "Q1", "A1", "", List.of(hint("h1"), hint("h2"), hint("h3"), hint("h4")));
        quiz.addQuestion(2, "Q2", "A2", "", List.of(hint("h1"), hint("h2"), hint("h3"), hint("h4")));
        quiz.addQuestion(3, "Q3", "A3", "", List.of(hint("h1"), hint("h2"), hint("h3"), hint("h4")));
        quiz.addQuestion(4, "Q4", "A4", "", List.of(hint("h1"), hint("h2"), hint("h3"), hint("h4")));
        quiz.addQuestion(5, "Q5", "", answerImageUrl, "", List.of(hint("h1"), hint("h2"), hint("h3")));
        for (int i = 6; i <= 8; i++) quiz.addQuestion(i, "Q" + i, "A" + i, "", textHints(3));
        return quiz;
    }

    private static Hint hint(String text) {
        Hint h = new Hint();
        h.setHintText(text);
        return h;
    }

    private static List<Hint> textHints(int count) {
        List<Hint> list = new ArrayList<>();
        for (int i = 0; i < count; i++) list.add(hint("h" + i));
        return list;
    }

    private CreateQuizRequest minimalUpdateRequest() {
        CreateQuizRequest req = new CreateQuizRequest();
        req.setPubDate(LocalDate.now());
        List<CreateQuizRequest.QuestionData> questionList = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            CreateQuizRequest.QuestionData qd = new CreateQuizRequest.QuestionData();
            qd.setNumber(i);
            qd.setQuestionText("Q" + i);
            qd.setAnswer("A" + i);
            qd.setNote("");
            int hintCount = i <= 4 ? 4 : 3;
            List<CreateQuizRequest.HintData> hdList = new ArrayList<>();
            for (int j = 0; j < hintCount; j++) {
                CreateQuizRequest.HintData hd = new CreateQuizRequest.HintData();
                hd.setHintText("hint");
                hdList.add(hd);
            }
            qd.setHints(hdList);
            questionList.add(qd);
        }
        req.setQuestions(questionList);
        return req;
    }

    @Test
    void updateQuizFull_deletesOldImageUrlAtStart() {
        Quiz existing = quizWithImageUrls("/uploads/old-start.jpg", null);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        quizService.updateQuizFull(1L, minimalUpdateRequest());

        verify(imageStorageService).delete("/uploads/old-start.jpg");
    }

    @Test
    void updateQuizFull_deletesOldImageUrlAsHint() {
        Quiz existing = quizWithImageUrls(null, "/uploads/old-hint.png");
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        quizService.updateQuizFull(1L, minimalUpdateRequest());

        verify(imageStorageService).delete("/uploads/old-hint.png");
    }

    @Test
    void updateQuizFull_doesNotDeleteImageUrl_whenKeptUnchanged() {
        String keptUrl = "/uploads/kept-image.jpg";
        Quiz existing = quizWithImageUrls(keptUrl, null);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        // Build a request that carries the same URL through (frontend passes it back)
        CreateQuizRequest req = minimalUpdateRequest();
        req.getQuestions().get(0).getHints().get(0).setImageUrlAtStart(keptUrl);

        quizService.updateQuizFull(1L, req);

        verify(imageStorageService, never()).delete(keptUrl);
    }

    @Test
    void updateQuizFull_deletesReplacedImageUrl_butKeepsOtherUnchanged() {
        String keptUrl = "/uploads/kept.jpg";
        String replacedUrl = "/uploads/replaced.png";
        // existing quiz: q1-h1 has both URLs
        Quiz existing = quizWithImageUrls(keptUrl, replacedUrl);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        // Request keeps atStart but replaces asHint with a new URL
        CreateQuizRequest req = minimalUpdateRequest();
        req.getQuestions().get(0).getHints().get(0).setImageUrlAtStart(keptUrl);
        req.getQuestions().get(0).getHints().get(0).setImageUrlAsHint("/uploads/new-hint.png");

        quizService.updateQuizFull(1L, req);

        verify(imageStorageService, never()).delete(keptUrl);
        verify(imageStorageService).delete(replacedUrl);
    }

    @Test
    void updateQuizFull_deletesOldAnswerImageUrl() {
        Quiz existing = quizWithAnswerImageUrl("/uploads/old-answer-q5.jpg");
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        quizService.updateQuizFull(1L, minimalUpdateRequest());

        verify(imageStorageService).delete("/uploads/old-answer-q5.jpg");
    }

    @Test
    void updateQuizFull_doesNotDeleteAnswerImageUrl_whenKeptUnchanged() {
        String keptAnswerUrl = "/uploads/kept-answer-q5.jpg";
        Quiz existing = quizWithAnswerImageUrl(keptAnswerUrl);
        when(quizRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(quizMapper.toDTO(any())).thenReturn(null);

        CreateQuizRequest req = minimalUpdateRequest();
        req.getQuestions().get(4).setAnswerImageUrl(keptAnswerUrl);

        quizService.updateQuizFull(1L, req);

        verify(imageStorageService, never()).delete(keptAnswerUrl);
    }
}
