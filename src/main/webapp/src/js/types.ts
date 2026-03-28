export interface HintDTO {
    hintText: string | null;
    imageUrl?: string | null;
}

export interface QuestionDTO {
    number: number;
    questionText: string;
    answer: string;
    note: string | null;
    hints: HintDTO[];
    imageUrl?: string | null;
}

export interface QuizDTO {
    quizId: number;
    title?: string;
    quizDate?: string;
    pubDate?: string;
    submitDate?: string;
    questionCount?: number;
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
