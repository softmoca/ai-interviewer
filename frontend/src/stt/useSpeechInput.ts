// 음성 입력 훅 — 결정사항 D37.
// STT 프로바이더를 켜고 끄며 인식 텍스트를 onTranscript로 흘려보낸다. 미지원 브라우저에서는
// supported=false만 반환하고 아무 동작도 하지 않는다(그레이스풀 폴백).

import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createSttProvider, isSpeechInputSupported } from './index';
import type { SttErrorCode, SttSession } from './types';

const ERROR_MESSAGES: Record<SttErrorCode, string> = {
  'not-allowed': '마이크 권한이 거부되었습니다. 브라우저 설정에서 허용해 주세요.',
  'no-speech': '음성이 감지되지 않았습니다. 다시 시도해 주세요.',
  'audio-capture': '마이크를 찾을 수 없습니다. 연결 상태를 확인해 주세요.',
  aborted: '', // 사용자가 멈춘 정상 종료 — 오류 메시지 없음
  network: '네트워크 문제로 음성 인식에 실패했습니다.',
  unknown: '음성 인식 중 문제가 발생했습니다.',
};

export interface SpeechInput {
  /** 이 브라우저가 음성 입력을 지원하는가(false면 마이크 UI를 숨긴다) */
  supported: boolean;
  /** 현재 녹음 중인지 */
  recording: boolean;
  /** 사용자에게 보여줄 오류 메시지(없으면 null) */
  error: string | null;
  start(): void;
  stop(): void;
}

/**
 * @param onTranscript 인식된 누적 텍스트를 받는 콜백(입력창을 채우는 용도).
 */
export function useSpeechInput(onTranscript: (text: string) => void): SpeechInput {
  const supported = useMemo(
    () => typeof window !== 'undefined' && isSpeechInputSupported(window),
    [],
  );
  const [recording, setRecording] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const sessionRef = useRef<SttSession | null>(null);
  // 최신 콜백을 참조해 start를 재생성하지 않는다(불필요한 리스너 재등록 방지).
  const onTranscriptRef = useRef(onTranscript);
  onTranscriptRef.current = onTranscript;

  const stop = useCallback(() => {
    sessionRef.current?.stop();
    sessionRef.current = null;
    setRecording(false);
  }, []);

  const start = useCallback(() => {
    if (!supported || sessionRef.current) return;
    const provider = createSttProvider(window);
    if (!provider) return; // 지원 감지 후에도 방어적으로 폴백
    setError(null);
    setRecording(true);
    sessionRef.current = provider.start({
      onTranscript: (result) => onTranscriptRef.current(result.text),
      onError: (code) => {
        const message = ERROR_MESSAGES[code];
        if (message) setError(message);
        sessionRef.current = null;
        setRecording(false);
      },
      onEnd: () => {
        sessionRef.current = null;
        setRecording(false);
      },
    });
  }, [supported]);

  // 언마운트 시 녹음이 남아있으면 정리
  useEffect(() => () => sessionRef.current?.stop(), []);

  return { supported, recording, error, start, stop };
}
