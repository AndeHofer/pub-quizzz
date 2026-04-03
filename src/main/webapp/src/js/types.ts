export interface HintDTO {
    hintText: string | null;
    imageUrlAtStart?: string | null;
    imageUrlAsHint?: string | null;
}

export interface QuestionDTO {
    number: number;
    questionText: string;
    answer: string;
    note: string | null;
    hints: HintDTO[];
}

export interface QuizDTO {
    quizId: number;
    title?: string;
    quizDate?: string;
    pubDate?: string;
    submitDate?: string;
    finished?: boolean;
    questions?: QuestionDTO[];
}

export interface TeamDTO {
    teamsId: number;
    teamName: string;
}

export interface AnswerScoreDTO {
    questionNumber: number;
    points: number;
    changed: boolean;
}

export interface ResultDTO {
    resultsId: number;
    quizId: number;
    teamId: number;
    teamName: string;
    quizDate: string;
    totalPoints: number;
    answers: AnswerScoreDTO[];
}

export interface UserDTO {
    userId: number;
    username: string;
    role: string;
}

export interface AllTimeLeaderboardEntry {
    rank: number;
    teamName: string;
    totalPoints: number;
    quizCount: number;
}

export interface TeamResultEntry {
    quizId: number;
    quizDate: string;
    quizTitle: string;
    totalPoints: number;
    answers: AnswerScoreDTO[];
}

export interface QuizSummaryDTO {
    quizId: number;
    quizTitle: string;
    pubDate: string;
    teamCount: number;
    winnerTeamName?: string | null;
}

export interface QuizResultEntry {
    rank: number;
    teamName: string;
    totalPoints: number;
    answers: AnswerScoreDTO[];
}

export interface QuizResultsResponse {
    quizTitle: string;
    entries: QuizResultEntry[];
}
