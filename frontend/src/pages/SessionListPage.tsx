import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../api/client';
import { listSessions } from '../api/session';
import type { SessionStatus, SessionSummary } from '../api/types';

const STATUS_LABEL: Record<SessionStatus, string> = {
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
  ABANDONED: '중단',
};

function formatTime(iso: string | null): string {
  if (!iso) return '-';
  const date = new Date(iso);
  return Number.isNaN(date.getTime()) ? iso : date.toLocaleString('ko-KR');
}

export function SessionListPage() {
  const [sessions, setSessions] = useState<SessionSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listSessions()
      .then((result) => {
        if (active) setSessions(result);
      })
      .catch((err) => {
        if (active) setError(err instanceof ApiError ? err.message : '면접 기록을 불러오지 못했습니다.');
      });
    return () => {
      active = false;
    };
  }, []);

  return (
    <div className="card wide">
      <h1>내 면접 기록</h1>
      {error && <p className="error">{error}</p>}
      {sessions === null && !error && <p className="muted">불러오는 중…</p>}
      {sessions && sessions.length === 0 && (
        <p className="muted">
          아직 면접 기록이 없습니다. <Link to="/sessions/new">새 면접 시작</Link>
        </p>
      )}
      {sessions && sessions.length > 0 && (
        <ul className="session-list">
          {sessions.map((session) => (
            <li key={session.sessionId}>
              <div>
                <span className={`badge ${session.status.toLowerCase()}`}>
                  {STATUS_LABEL[session.status]}
                </span>
                <span className="muted"> {formatTime(session.startedAt)}</span>
              </div>
              <Link to={`/sessions/${session.sessionId}`}>
                {session.status === 'IN_PROGRESS' ? '이어하기' : '다시보기'} →
              </Link>
            </li>
          ))}
        </ul>
      )}
      <p className="back">
        <Link to="/">← 홈</Link>
      </p>
    </div>
  );
}
