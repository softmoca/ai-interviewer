import { describe, expect, it, vi } from 'vitest';
import {
  createWebSpeechProvider,
  getSpeechRecognitionCtor,
  isSpeechInputSupported,
} from './webSpeech';

// 가짜 SpeechRecognition — 이벤트 핸들러를 수동으로 트리거해 어댑터 배선을 검증한다.
class FakeRecognition {
  lang = '';
  interimResults = false;
  continuous = true;
  maxAlternatives = 0;
  onresult: ((e: unknown) => void) | null = null;
  onerror: ((e: unknown) => void) | null = null;
  onend: (() => void) | null = null;
  start = vi.fn();
  stop = vi.fn();
  abort = vi.fn();
}

function fakeWin(overrides: Record<string, unknown>): Window {
  return overrides as unknown as Window;
}

// SpeechRecognitionEvent 흉내 — 배열형 인덱스 접근 구조를 items로 만든다.
function resultsEvent(items: Array<{ transcript: string; isFinal: boolean }>, resultIndex = 0) {
  const results: Record<number | 'length', unknown> = { length: items.length };
  items.forEach((it, i) => {
    results[i] = { 0: { transcript: it.transcript }, length: 1, isFinal: it.isFinal };
  });
  return { resultIndex, results };
}

describe('isSpeechInputSupported', () => {
  it('표준 SpeechRecognition이 있으면 지원으로 본다', () => {
    expect(isSpeechInputSupported(fakeWin({ SpeechRecognition: FakeRecognition }))).toBe(true);
  });

  it('webkit 프리픽스만 있어도 지원으로 본다', () => {
    expect(isSpeechInputSupported(fakeWin({ webkitSpeechRecognition: FakeRecognition }))).toBe(true);
  });

  it('둘 다 없으면 미지원(폴백)', () => {
    expect(isSpeechInputSupported(fakeWin({}))).toBe(false);
    expect(getSpeechRecognitionCtor(fakeWin({}))).toBeNull();
  });
});

describe('createWebSpeechProvider', () => {
  it('미지원 브라우저에서는 null을 반환한다(그레이스풀 폴백)', () => {
    expect(createWebSpeechProvider(fakeWin({}))).toBeNull();
  });

  it('start() 시 인식을 시작하고 인식 텍스트를 onTranscript로 흘려보낸다', () => {
    const instances: FakeRecognition[] = [];
    class Tracking extends FakeRecognition {
      constructor() {
        super();
        instances.push(this);
      }
    }
    const provider = createWebSpeechProvider(fakeWin({ SpeechRecognition: Tracking }), 'ko-KR');
    expect(provider).not.toBeNull();

    const onTranscript = vi.fn();
    const session = provider!.start({ onTranscript, onError: vi.fn(), onEnd: vi.fn() });

    const rec = instances[0];
    expect(rec.start).toHaveBeenCalled();
    expect(rec.lang).toBe('ko-KR');
    expect(rec.interimResults).toBe(true);
    expect(rec.continuous).toBe(true);

    rec.onresult?.(resultsEvent([{ transcript: '스택은', isFinal: false }]));
    expect(onTranscript).toHaveBeenCalledWith({ text: '스택은', isFinal: false });

    session.stop();
    expect(rec.stop).toHaveBeenCalled();
  });

  it('확정 텍스트는 이어지는 발화에 누적되고, 중간 결과는 뒤에 임시로 붙는다', () => {
    const instances: FakeRecognition[] = [];
    class Tracking extends FakeRecognition {
      constructor() {
        super();
        instances.push(this);
      }
    }
    const provider = createWebSpeechProvider(fakeWin({ SpeechRecognition: Tracking }));
    const onTranscript = vi.fn();
    provider!.start({ onTranscript, onError: vi.fn(), onEnd: vi.fn() });
    const rec = instances[0];

    rec.onresult?.(resultsEvent([{ transcript: '스택은 ', isFinal: true }]));
    expect(onTranscript).toHaveBeenLastCalledWith({ text: '스택은 ', isFinal: true });

    // 다음 이벤트(resultIndex=1)의 중간 결과가 앞선 확정분에 이어붙는다.
    rec.onresult?.(resultsEvent(
      [
        { transcript: '스택은 ', isFinal: true },
        { transcript: '배열', isFinal: false },
      ],
      1,
    ));
    expect(onTranscript).toHaveBeenLastCalledWith({ text: '스택은 배열', isFinal: false });
  });

  it('사용자가 멈추기 전 브라우저가 끊기면(onend) 자동으로 다시 시작한다', () => {
    const instances: FakeRecognition[] = [];
    class Tracking extends FakeRecognition {
      constructor() {
        super();
        instances.push(this);
      }
    }
    const provider = createWebSpeechProvider(fakeWin({ SpeechRecognition: Tracking }));
    const onEnd = vi.fn();
    const session = provider!.start({ onTranscript: vi.fn(), onError: vi.fn(), onEnd });
    const rec = instances[0];
    expect(rec.start).toHaveBeenCalledTimes(1);

    // 브라우저가 세션을 끊음 → 사용자가 멈춘 게 아니므로 재시작, onEnd는 아직 호출 안 함
    rec.onend?.();
    expect(rec.start).toHaveBeenCalledTimes(2);
    expect(onEnd).not.toHaveBeenCalled();

    // 사용자가 멈춘 뒤 끊기면 재시작하지 않고 onEnd로 종료를 알린다
    session.stop();
    rec.onend?.();
    expect(rec.start).toHaveBeenCalledTimes(2);
    expect(onEnd).toHaveBeenCalledTimes(1);
  });

  it('권한 거부(not-allowed) 오류를 onError로 전달한다', () => {
    const instances: FakeRecognition[] = [];
    class Tracking extends FakeRecognition {
      constructor() {
        super();
        instances.push(this);
      }
    }
    const provider = createWebSpeechProvider(fakeWin({ SpeechRecognition: Tracking }));
    const onError = vi.fn();
    provider!.start({ onTranscript: vi.fn(), onError, onEnd: vi.fn() });

    instances[0].onerror?.({ error: 'not-allowed' });
    expect(onError).toHaveBeenCalledWith('not-allowed');
  });
});
