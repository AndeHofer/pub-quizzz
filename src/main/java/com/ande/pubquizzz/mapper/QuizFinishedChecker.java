package com.ande.pubquizzz.mapper;

import com.ande.pubquizzz.database.entities.Hint;
import com.ande.pubquizzz.database.entities.Question;
import com.ande.pubquizzz.database.entities.Quiz;

import java.util.List;

public class QuizFinishedChecker {

    private QuizFinishedChecker() {}

    /**
     * A quiz is "finished" when:
     * - It has exactly 8 questions
     * - Every question has a non-blank questionText
     * - Questions 1-4 need non-blank answer text
     * - Questions 5-8 need non-blank answer text OR answerImageUrl
     * - Every hint has either a non-blank hintText OR a non-null imageUrlAsHint
     *   (imageUrlAtStart alone does NOT count as a filled hint)
     */
    public static boolean isFinished(Quiz quiz) {
        List<Question> questions = quiz.getQuestions();
        if (questions == null || questions.size() != 8) return false;
        for (Question q : questions) {
            if (q.getQuestionText() == null || q.getQuestionText().isBlank()) return false;
            int qNum = q.getId().getQuestionNumber();
            boolean hasAnswerText = q.getAnswer() != null && !q.getAnswer().isBlank();
            boolean hasAnswerImage = q.getAnswerImageUrl() != null;
            if (qNum >= 1 && qNum <= 4) {
                if (!hasAnswerText) return false;
            } else {
                if (!hasAnswerText && !hasAnswerImage) return false;
            }
            int expectedHints = (qNum >= 1 && qNum <= 4) ? 4 : 3;
            List<Hint> hints = q.getHints();
            if (hints == null || hints.size() != expectedHints) return false;
            for (Hint h : hints) {
                boolean hintFilled = (h.getHintText() != null && !h.getHintText().isBlank())
                        || h.getImageUrlAsHint() != null;
                if (!hintFilled) return false;
            }
        }
        return true;
    }
}
