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

// SpeechRecognitionEvent.results 흉내 — 배열형 인덱스 접근 구조.
function fakeResults(text: string, isFinal: boolean) {
  const result = { 0: { transcript: text }, length: 1, isFinal };
  return { resultIndex: 0, results: { 0: result, length: 1 } };
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

    rec.onresult?.(fakeResults('스택은', false));
    expect(onTranscript).toHaveBeenCalledWith({ text: '스택은', isFinal: false });

    session.stop();
    expect(rec.stop).toHaveBeenCalled();
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
