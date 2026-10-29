import {describe, expect, it} from 'vitest';

import {findNextQuiz} from './quiz-navigation';
import type {QuizSummaryDTO} from './types';

const quizzes: QuizSummaryDTO[] = [
    {quizId: 1, quizTitle: '2025 November', pubDate: '2025-11-15', finished: true, teamCount: 3},
    {quizId: 2, quizTitle: '2026 Jänner', pubDate: '2026-01-17', finished: true, teamCount: 4},
    {quizId: 3, quizTitle: '2026 Februar', pubDate: '2026-02-14', finished: true, teamCount: 5},
    {quizId: 4, quizTitle: 'Entwurf', pubDate: '2026-03-14', finished: false, teamCount: 0},
];

describe('next quiz navigation', () => {
    it('selects the chronologically previous finished quiz', () => {
        expect(findNextQuiz(3, quizzes)?.quizId).toBe(2);
    });

    it('wraps from the oldest finished quiz to the newest', () => {
        expect(findNextQuiz(1, quizzes)?.quizId).toBe(3);
    });

    it('does not navigate to an unfinished quiz', () => {
        expect(findNextQuiz(4, quizzes)).toBeNull();
    });

    it('does not return a self-link when only one finished quiz exists', () => {
        const onlyQuiz = [quizzes[0]];

        expect(findNextQuiz(1, onlyQuiz)).toBeNull();
    });

    it('does not return a quiz when the current quiz is unavailable', () => {
        expect(findNextQuiz(99, quizzes)).toBeNull();
    });
});
