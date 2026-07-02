import { useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../api/client';
import { generateEvaluation } from '../api/evaluation';
import type { EvaluationReport } from '../api/types';

export function EvaluationReportPage() {
  const { id } = useParams();
  const sessionId = Number(id);

  const [report, setReport] = useState<EvaluationReport | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  // 평가 생성 POST가 한 번만 나가도록 가드(React StrictMode의 이중 마운트로 중복 생성되던 문제 방지)
  const requestedSessionId = useRef<number | null>(null);

  useEffect(() => {
    if (!Number.isFinite(sessionId)) {
      setError('잘못된 세션입니다.');
      setLoading(false);
      return;
    }
    if (requestedSessionId.current === sessionId) {
      return; // 같은 세션에 대한 중복 요청 방지
    }
    requestedSessionId.current = sessionId;

    let active = true;
    // 멱등 생성: 최초엔 LLM으로 평가 생성, 이후엔 저장된 리포트 반환
    generateEvaluation(sessionId)
      .then((result) => {
        if (active) setReport(result);
      })
      .catch((err) => {
        if (active) setError(err instanceof ApiError ? err.message : '평가 리포트를 불러오지 못했습니다.');
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [sessionId]);

  if (loading) {
    return <p className="centered">평가 리포트를 생성하는 중입니다… (LLM 호출)</p>;
  }
  if (error) {
    return (
      <div className="card">
        <h1>평가 리포트</h1>
        <p className="error">{error}</p>
        <p className="back">
          <Link to="/">← 홈</Link>
        </p>
      </div>
    );
  }
  if (!report) {
    return null;
  }

  return (
    <div className="card wide">
      <h1>평가 리포트</h1>

      {report.overallComment && (
        <section className="overall">
          <h2>총평</h2>
          <p>{report.overallComment}</p>
        </section>
      )}

      <section>
        <h2>개념별 평가</h2>
        {report.concepts.map((concept, index) => (
          <article key={`${concept.concept}-${index}`} className="concept">
            <h3>{concept.concept}</h3>
            <p className="scores">
              정확성 {concept.accuracy}/5 · 깊이 {concept.depth}/5
            </p>
            {concept.missedKeywords.length > 0 && (
              <p className="keywords">
                놓친 키워드:{' '}
                {concept.missedKeywords.map((keyword) => (
                  <span key={keyword} className="chip">
                    {keyword}
                  </span>
                ))}
              </p>
            )}
            {concept.modelAnswer && (
              <details>
                <summary>모범답안</summary>
                <p>{concept.modelAnswer}</p>
              </details>
            )}
          </article>
        ))}
      </section>

      <p className="back">
        <Link to="/">← 홈</Link>
      </p>
    </div>
  );
}
