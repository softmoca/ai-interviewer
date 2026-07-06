// Web Speech API 기반 STT 구현(어댑터) — 결정사항 D37.
// 브라우저 내장 SpeechRecognition을 SttProvider 포트로 감싼다. 비용·서버 불필요한 MVP 구현.

import type { SttErrorCode, SttHandlers, SttProvider, SttSession } from './types';

// --- Web Speech API 최소 타입 ---
// lib.dom.d.ts에 SpeechRecognition 표준 타입이 포함된다는 보장이 없고(벤더 프리픽스),
// 우리가 쓰는 표면만 필요하므로 최소한으로 직접 선언한다.
interface SpeechRecognitionAlternative {
  readonly transcript: string;
}
interface SpeechRecognitionResult {
  readonly length: number;
  readonly isFinal: boolean;
  readonly [index: number]: SpeechRecognitionAlternative;
}
interface SpeechRecognitionResultList {
  readonly length: number;
  readonly [index: number]: SpeechRecognitionResult;
}
interface SpeechRecognitionEvent {
  readonly resultIndex: number;
  readonly results: SpeechRecognitionResultList;
}
interface SpeechRecognitionErrorEvent {
  readonly error: string;
}
interface SpeechRecognitionInstance {
  lang: string;
  interimResults: boolean;
  continuous: boolean;
  maxAlternatives: number;
  onresult: ((event: SpeechRecognitionEvent) => void) | null;
  onerror: ((event: SpeechRecognitionErrorEvent) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
  abort(): void;
}
type SpeechRecognitionCtor = new () => SpeechRecognitionInstance;

/** 표준/webkit 프리픽스 어느 쪽이든 SpeechRecognition 생성자를 찾아 반환(없으면 null). */
export function getSpeechRecognitionCtor(win: Window): SpeechRecognitionCtor | null {
  const w = win as unknown as {
    SpeechRecognition?: SpeechRecognitionCtor;
    webkitSpeechRecognition?: SpeechRecognitionCtor;
  };
  return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
}

/** 이 브라우저가 음성 인식을 지원하는가(마이크 버튼 노출 여부 판단에 사용). */
export function isSpeechInputSupported(win: Window): boolean {
  return getSpeechRecognitionCtor(win) !== null;
}

function toErrorCode(raw: string): SttErrorCode {
  switch (raw) {
    case 'not-allowed':
    case 'service-not-allowed':
      return 'not-allowed';
    case 'no-speech':
      return 'no-speech';
    case 'audio-capture':
      return 'audio-capture';
    case 'aborted':
      return 'aborted';
    case 'network':
      return 'network';
    default:
      return 'unknown';
  }
}

/**
 * Web Speech API 기반 STT 프로바이더를 만든다. 미지원 브라우저면 null을 반환하므로
 * 호출부에서 그레이스풀 폴백(마이크 버튼 숨김)을 할 수 있다.
 */
export function createWebSpeechProvider(win: Window, lang = 'ko-KR'): SttProvider | null {
  const Ctor = getSpeechRecognitionCtor(win);
  if (!Ctor) return null;

  return {
    start(handlers: SttHandlers): SttSession {
      const recognition = new Ctor();
      recognition.lang = lang;
      recognition.interimResults = true; // 말하는 즉시 초안이 채워지도록 중간 결과 수신
      recognition.continuous = false; // 한 번의 발화 단위(사용자가 멈추면 종료)
      recognition.maxAlternatives = 1;

      recognition.onresult = (event) => {
        // 세션 시작 후 모든 조각을 누적해 현재까지의 전체 텍스트를 만든다.
        let text = '';
        let isFinal = true;
        for (let i = 0; i < event.results.length; i += 1) {
          text += event.results[i][0].transcript;
          if (!event.results[i].isFinal) isFinal = false;
        }
        handlers.onTranscript({ text, isFinal });
      };
      recognition.onerror = (event) => handlers.onError(toErrorCode(event.error));
      recognition.onend = () => handlers.onEnd();

      recognition.start();
      return {
        stop() {
          recognition.stop();
        },
      };
    },
  };
}
