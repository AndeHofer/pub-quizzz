export interface QuizDTO {
  quizId: number;
  pubDate: string;
  submitDate: string;
  questionCount: number;
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

export interface CreateResultRequest {
  quizId: number;
  teamId: number;
  answers: AnswerSubmission[];
}

export interface AnswerSubmission {
  questionNumber: number;
  points: number;
}

export interface ResultDTO {
  resultsId: number;
  teamId: number;
  teamName: string;
  quizId: number;
  quizDate: string;
  answers: AnswerScoreDTO[];
  totalPoints: number;
}

export interface LeaderboardEntry {
  rank: number;
  teamName: string;
  teamId: number;
  quizId: number;
  quizDate: string;
  totalPoints: number;
}

export interface UserDTO {
  userId: number;
  username: string;
  role: string;
}
