import { useEffect, useState, type FormEvent } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { completeSession, getSession, submitAnswer } from '../api/session';
import type { QaLogEntry, SessionStatus } from '../api/types';

/** 답변되지 않은 첫 면접관 질문(선형 흐름의 현재 질문). 없으면 null. */
function firstPendingQuestion(transcript: QaLogEntry[]): string | null {
  let lastUserIndex = -1;
  transcript.forEach((entry, index) => {
    if (entry.role === 'USER') lastUserIndex = index;
  });
  const pending = transcript.slice(lastUserIndex + 1).find((e) => e.role === 'INTERVIEWER');
  return pending ? pending.content : null;
}

export function InterviewPage() {
  const { id } = useParams();
  const sessionId = Number(id);

  const [status, setStatus] = useState<SessionStatus | null>(null);
  const [transcript, setTranscript] = useState<QaLogEntry[]>([]);
  const [answer, setAnswer] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!Number.isFinite(sessionId)) {
      setError('잘못된 세션입니다.');
      setLoading(false);
      return;
    }
    let active = true;
    getSession(sessionId)
      .then((session) => {
        if (active) {
          setStatus(session.status);
          setTranscript(session.transcript);
        }
      })
      .catch((err) => {
        if (active) setError(err instanceof ApiError ? err.message : '세션을 불러오지 못했습니다.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [sessionId]);

  const currentQuestion = firstPendingQuestion(transcript);

  async function handleAnswer(event: FormEvent) {
    event.preventDefault();
    const content = answer.trim();
    if (!content) return;
    setError(null);
    setBusy(true);
    try {
      const result = await submitAnswer(sessionId, content);
      const lastSeq = transcript.length ? transcript[transcript.length - 1].seq : 0;
      const appended: QaLogEntry[] = [
        { seq: lastSeq + 1, role: 'USER', content, followUp: false },
        ...result.followUps.map((f) => ({
          seq: f.seq,
          role: 'INTERVIEWER' as const,
          content: f.content,
          followUp: true,
        })),
      ];
      setTranscript((prev) => [...prev, ...appended]);
      setStatus(result.status);
      setAnswer('');
    } catch (err) {
      // LLM 미설정(503) 등은 서버에서 답변까지 롤백되므로 그대로 재시도 가능
      setError(err instanceof ApiError ? err.message : '답변 제출에 실패했습니다.');
    } finally {
      setBusy(false);
    }
  }

  async function handleComplete() {
    setError(null);
    setBusy(true);
    try {
      const result = await completeSession(sessionId);
      setStatus(result.status);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '세션 종료에 실패했습니다.');
    } finally {
      setBusy(false);
    }
  }

  if (loading) {
    return <p className="centered">불러오는 중…</p>;
  }

  return (
    <div className="card wide">
      <h1>면접 진행</h1>
      {error && <p className="error">{error}</p>}

      <ol className="transcript">
        {transcript.map((entry) => (
          <li key={entry.seq} className={`turn ${entry.role === 'USER' ? 'user' : 'interviewer'}`}>
            <span className="speaker">
              {entry.role === 'USER' ? '나' : '면접관'}
              {entry.followUp ? ' · 꼬리질문' : ''}
            </span>
            <p>{entry.content}</p>
          </li>
        ))}
      </ol>

      {status === 'COMPLETED' ? (
        <div className="done">
          <p>면접이 종료되었습니다. 수고하셨습니다! 🎉</p>
          <p className="muted">평가 리포트 화면은 다음 슬라이스에서 연결됩니다.</p>
          <Link to="/">홈으로</Link>
        </div>
      ) : (
        <>
          {currentQuestion ? (
            <form onSubmit={handleAnswer}>
              <label>
                답변
                <textarea
                  value={answer}
                  onChange={(e) => setAnswer(e.target.value)}
                  rows={5}
                  required
                />
              </label>
              <button type="submit" disabled={busy}>
                {busy ? '제출 중…' : '답변 제출'}
              </button>
            </form>
          ) : (
            <p className="muted">더 이상 꼬리질문이 없습니다. 면접을 종료하세요.</p>
          )}
          <button type="button" className="secondary" onClick={handleComplete} disabled={busy}>
            면접 종료
          </button>
        </>
      )}

      <p className="back">
        <Link to="/">← 홈</Link>
      </p>
    </div>
  );
}
