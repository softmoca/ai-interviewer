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
      recognition.continuous = true; // 침묵으로 자동 종료하지 않고, 사용자가 멈출 때까지 계속 듣는다
      recognition.maxAlternatives = 1;

      // 확정(final) 텍스트는 재시작을 넘어 계속 누적한다. 브라우저가 세션을 끊고 다시
      // 시작하면 results 목록이 초기화되므로, 확정분을 여기 모아 두어야 이어붙일 수 있다.
      let finalText = '';
      let stopped = false; // 사용자가 명시적으로 멈췄는가(자동 재시작 여부 판단)

      recognition.onresult = (event) => {
        // 이번 이벤트의 새 결과(resultIndex부터)만 훑어 확정분은 누적, 중간분은 임시로 붙인다.
        let interim = '';
        for (let i = event.resultIndex; i < event.results.length; i += 1) {
          const result = event.results[i];
          if (result.isFinal) finalText += result[0].transcript;
          else interim += result[0].transcript;
        }
        handlers.onTranscript({ text: finalText + interim, isFinal: interim === '' });
      };

      recognition.onerror = (event) => {
        const code = toErrorCode(event.error);
        // 치명 오류(권한 거부·마이크 없음)만 사용자에게 알리고 종료한다. no-speech·network 등
        // 일시적 오류는 onend의 자동 재시작에 맡겨 계속 듣게 한다.
        if (code === 'not-allowed' || code === 'audio-capture') {
          stopped = true;
          handlers.onError(code);
        }
      };

      recognition.onend = () => {
        if (stopped) {
          handlers.onEnd();
          return;
        }
        // 브라우저가 침묵/세션 길이 한계로 끊어도 사용자가 멈출 때까지 이어서 듣는다.
        try {
          recognition.start();
        } catch {
          stopped = true;
          handlers.onEnd();
        }
      };

      recognition.start();
      return {
        stop() {
          stopped = true;
          recognition.stop();
        },
      };
    },
  };
}
