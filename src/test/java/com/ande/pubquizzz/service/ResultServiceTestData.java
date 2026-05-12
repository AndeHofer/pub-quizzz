package com.ande.pubquizzz.service;

import com.ande.pubquizzz.database.entities.ResultAnswer;
import com.ande.pubquizzz.dto.CreateResultRequest;
import com.ande.pubquizzz.dto.UpdateResultRequest;

import java.util.ArrayList;
import java.util.List;

final class ResultServiceTestData {

    private ResultServiceTestData() {
    }

    static List<CreateResultRequest.AnswerSubmission> createAnswerSubmissions(int... pointsByQuestion) {
        List<CreateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 0; i < pointsByQuestion.length; i++) {
            CreateResultRequest.AnswerSubmission submission = new CreateResultRequest.AnswerSubmission();
            submission.setQuestionNumber(i + 1);
            submission.setPoints(pointsByQuestion[i]);
            submissions.add(submission);
        }
        return submissions;
    }

    static List<UpdateResultRequest.AnswerSubmission> updateAnswerSubmissions(int... pointsByQuestion) {
        List<UpdateResultRequest.AnswerSubmission> submissions = new ArrayList<>();
        for (int i = 0; i < pointsByQuestion.length; i++) {
            UpdateResultRequest.AnswerSubmission submission = new UpdateResultRequest.AnswerSubmission();
            submission.setQuestionNumber(i + 1);
            submission.setPoints(pointsByQuestion[i]);
            submissions.add(submission);
        }
        return submissions;
    }

    static List<ResultAnswer> resultAnswersWithDefaultPoints(int points) {
        List<ResultAnswer> answers = new ArrayList<>();
        for (int i = 1; i <= 8; i++) {
            ResultAnswer answer = new ResultAnswer();
            answer.setQuestionNumber(i);
            answer.setPoints(points);
            answer.setChanged(false);
            answers.add(answer);
        }
        return answers;
    }
}
