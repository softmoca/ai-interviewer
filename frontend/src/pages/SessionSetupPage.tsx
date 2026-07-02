import { useEffect, useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { listCategories } from '../api/category';
import { ApiError } from '../api/client';
import { startSession } from '../api/session';
import type { Category } from '../api/types';

export function SessionSetupPage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState<Category[]>([]);
  const [selected, setSelected] = useState<string[]>([]);
  const [randomAll, setRandomAll] = useState(false);
  const [difficulty, setDifficulty] = useState<string>(''); // '' = 혼합
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    listCategories()
      .then(setCategories)
      .catch((err) => setError(err instanceof ApiError ? err.message : '카테고리를 불러오지 못했습니다.'));
  }, []);

  function toggle(slug: string) {
    setSelected((prev) =>
      prev.includes(slug) ? prev.filter((s) => s !== slug) : [...prev, slug],
    );
  }

  async function handleSubmit(event: FormEvent) {
    event.preventDefault();
    if (!randomAll && selected.length === 0) {
      setError('카테고리를 하나 이상 선택하거나 전체 랜덤을 켜세요.');
      return;
    }
    setError(null);
    setSubmitting(true);
    try {
      const result = await startSession({
        categorySlugs: randomAll ? undefined : selected,
        randomAll,
        difficulty: difficulty === '' ? null : Number(difficulty),
      });
      navigate(`/sessions/${result.sessionId}`);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : '세션을 시작하지 못했습니다.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="card">
      <h1>새 면접 시작</h1>
      <form onSubmit={handleSubmit}>
        <label className="checkbox">
          <input
            type="checkbox"
            checked={randomAll}
            onChange={(e) => setRandomAll(e.target.checked)}
          />
          전체 랜덤 (모든 카테고리에서 출제)
        </label>

        {!randomAll && (
          <fieldset disabled={submitting}>
            <legend>카테고리 선택</legend>
            {categories.length === 0 && <p className="muted">카테고리를 불러오는 중…</p>}
            {categories.map((c) => (
              <label key={c.slug} className="checkbox">
                <input
                  type="checkbox"
                  checked={selected.includes(c.slug)}
                  onChange={() => toggle(c.slug)}
                />
                {c.name}
              </label>
            ))}
          </fieldset>
        )}

        <label>
          난이도
          <select value={difficulty} onChange={(e) => setDifficulty(e.target.value)}>
            <option value="">혼합</option>
            <option value="1">1 (기본)</option>
            <option value="2">2 (비교·이유)</option>
            <option value="3">3 (세부 메커니즘)</option>
          </select>
        </label>

        {error && <p className="error">{error}</p>}
        <button type="submit" disabled={submitting}>
          {submitting ? '시작 중…' : '면접 시작'}
        </button>
      </form>
    </div>
  );
}
