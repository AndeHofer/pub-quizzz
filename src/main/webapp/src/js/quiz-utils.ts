import type {QuizSummaryDTO} from './types';

const GERMAN_MONTHS = [
    'Jänner',
    'Februar',
    'März',
    'April',
    'Mai',
    'Juni',
    'Juli',
    'August',
    'September',
    'Oktober',
    'November',
    'Dezember'
];

export type QuizWithDateAndId = {
    quizId: number;
    pubDate?: string;
};

export type QuizWithFinishedFlag = {
    finished?: boolean;
};

export function parsePubDateToMillis(pubDate?: string): number | null {
    if (!pubDate) return null;
    const parsed = Date.parse(`${pubDate}T00:00:00Z`);
    return Number.isNaN(parsed) ? null : parsed;
}

export function compareQuizzesNewestFirst<T extends QuizWithDateAndId>(left: T, right: T): number {
    const leftDate = parsePubDateToMillis(left.pubDate);
    const rightDate = parsePubDateToMillis(right.pubDate);

    if (leftDate !== null && rightDate !== null) {
        if (leftDate !== rightDate) return rightDate - leftDate;
    } else if (leftDate !== null) {
        return -1;
    } else if (rightDate !== null) {
        return 1;
    }

    return right.quizId - left.quizId;
}

export function quizDisplayTitle(quiz: QuizWithDateAndId): string {
    if (quiz.pubDate) {
        const parts = quiz.pubDate.split('-');
        if (parts.length >= 2) {
            const year = parts[0];
            const month = Number(parts[1]);
            if (month >= 1 && month <= 12) {
                return `${year} ${GERMAN_MONTHS[month - 1]}`;
            }
        }
    }
    return `Quiz ${quiz.quizId}`;
}

export function filterFinishedQuizzes<T extends QuizWithFinishedFlag>(quizzes: T[]): T[] {
    return quizzes.filter(quiz => quiz.finished === true);
}

export function sortQuizzesNewestFirst<T extends QuizWithDateAndId>(quizzes: T[]): T[] {
    return quizzes.slice().sort(compareQuizzesNewestFirst);
}

export function sortFinishedQuizzesNewestFirst(quizzes: QuizSummaryDTO[]): QuizSummaryDTO[] {
    return sortQuizzesNewestFirst(filterFinishedQuizzes(quizzes));
}
