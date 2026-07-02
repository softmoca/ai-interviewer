// 백엔드 응답 계약 (docs/아키텍처.md 4.5 인증 API와 정합)

export interface SignupResponse {
  id: number;
  email: string;
  nickname: string;
}

export interface TokenResponse {
  accessToken: string;
  tokenType: string;
  userId: number;
  nickname: string;
}

export interface MeResponse {
  userId: number;
  email: string;
  nickname: string;
}

/** GlobalExceptionHandler의 표준 에러 바디 */
export interface ErrorResponse {
  code: string;
  message: string;
}

// --- 카테고리 / 세션 (docs/아키텍처.md 4.6, 2.6) ---

export interface Category {
  slug: string;
  name: string;
  phase: string;
}

export type SessionStatus = 'IN_PROGRESS' | 'COMPLETED' | 'ABANDONED';
export type QaRole = 'INTERVIEWER' | 'USER';

export interface QuestionView {
  questionId: number;
  content: string;
  difficulty: number;
  seq: number;
}

export interface StartSessionResponse {
  sessionId: number;
  status: SessionStatus;
  firstQuestion: QuestionView;
}

export interface FollowUpView {
  qaLogId: number;
  seq: number;
  content: string;
}

export interface AnswerResponse {
  answerLogId: number;
  answerSeq: number;
  followUps: FollowUpView[];
  status: SessionStatus;
}

export interface QaLogEntry {
  seq: number;
  role: QaRole;
  content: string;
  followUp: boolean;
}

export interface SessionDetail {
  sessionId: number;
  status: SessionStatus;
  startedAt: string | null;
  endedAt: string | null;
  transcript: QaLogEntry[];
}

export interface SessionStatusResponse {
  sessionId: number;
  status: SessionStatus;
  endedAt: string | null;
}

/** 세션 목록(내 면접 기록) 항목 */
export interface SessionSummary {
  sessionId: number;
  status: SessionStatus;
  startedAt: string | null;
  endedAt: string | null;
}

// --- 평가 리포트 (docs/아키텍처.md 5.2, 결정사항 D28) ---

export interface ConceptScore {
  concept: string;
  accuracy: number; // 1~5
  depth: number; // 1~5
  missedKeywords: string[];
  modelAnswer: string | null;
}

export interface EvaluationReport {
  sessionId: number;
  concepts: ConceptScore[];
  overallComment: string | null;
}
