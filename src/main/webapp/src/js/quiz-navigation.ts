import type {QuizSummaryDTO} from './types';
import {sortFinishedQuizzesNewestFirst} from './quiz-utils';

export function findNextQuiz(currentQuizId: number, quizzes: QuizSummaryDTO[]): QuizSummaryDTO | null {
    const finishedQuizzes = sortFinishedQuizzesNewestFirst(quizzes);
    if (finishedQuizzes.length < 2) return null;

    const currentQuizIndex = finishedQuizzes.findIndex(quiz => quiz.quizId === currentQuizId);
    if (currentQuizIndex === -1) return null;

    return finishedQuizzes[(currentQuizIndex + 1) % finishedQuizzes.length];
}
