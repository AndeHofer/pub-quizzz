export interface HintDTO {
    hintText: string | null;
    imageUrlAtStart?: string | null;
    imageUrlAsHint?: string | null;
}

export interface QuestionDTO {
    number: number;
    questionText: string;
    answer: string;
    answerImageUrl?: string | null;
    note: string | null;
    hints: HintDTO[];
}

export interface QuizDTO {
    quizId: number;
    quizDate?: string;
    pubDate?: string;
    submitDate?: string;
    creator?: string | null;
    finished?: boolean;
    questions?: QuestionDTO[];
}

export interface QuizDetailResponse {
    quizId: number;
    pubDate?: string;
    submitDate?: string;
    creator?: string | null;
    questions?: QuestionDTO[];
}

export interface TeamDTO {
    teamsId: number;
    teamName: string;
}

export interface AnswerScoreDTO {
    questionNumber: number;
    points: number;
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

export interface AdminMonthlyLoginStatDTO {
    month: string;
    role: string;
    loginCount: number;
}

export interface AdminLogEntryDTO {
    timestamp: string | null;
    level: string;
    source: string | null;
    message: string | null;
    rawLine: string;
}

export interface AdminLogResponseDTO {
    entries: AdminLogEntryDTO[];
    appliedLimit: number;
    returnedCount: number;
}

export interface AllTimeLeaderboardEntry {
    rank: number;
    teamId: number;
    teamName: string;
    totalPoints: number;
    quizCount: number;
}

export interface MedalLeaderboardEntry {
    rank: number;
    teamId: number;
    teamName: string;
    goldCount: number;
    silverCount: number;
    bronzeCount: number;
}

export interface AverageLeaderboardEntry {
    rank: number;
    teamId: number;
    teamName: string;
    averagePoints: number;
    quizCount: number;
}

export interface TopResultLeaderboardEntry {
    rank: number;
    teamId: number;
    teamName: string;
    quizId: number;
    quizTitle: string;
    totalPoints: number;
    quizRank: number;
}

export interface TeamResultEntry {
    quizRank: number;
    participantCount: number;
    teamId: number;
    teamName: string;
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
    finished?: boolean;
    teamCount: number;
    winnerTeamId?: number | null;
    winnerTeamName?: string | null;
}

export interface QuizResultEntry {
    rank: number;
    teamId: number;
    teamName: string;
    totalPoints: number;
    answers: AnswerScoreDTO[];
}

export interface QuizResultsResponse {
    quizTitle: string;
    entries: QuizResultEntry[];
}

export interface QuizDocumentDTO {
    id: number;
    quizId: number;
    originalFilename: string;
    contentType: string;
    fileSize: number;
    uploadedAt: string;
}

export interface NewsDTO {
    newsId: number;
    title: string;
    text: string;
    createdAt: string;
    showOnHomePage: boolean;
}
