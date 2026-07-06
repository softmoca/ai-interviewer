// 음성 입력(STT) 포트 — 결정사항 D37.
// MVP는 브라우저 Web Speech API로 구현하지만, 나중에 Whisper/클라우드 STT 어댑터로
// 교체할 수 있게 특정 구현에 코드를 묶지 않고 이 인터페이스 뒤로 감춘다.

/** 인식된 텍스트 한 조각. 중간 결과(interim)일 수도, 확정(final)일 수도 있다. */
export interface SttTranscript {
  /** 세션 시작 후 지금까지 인식된 누적 텍스트 */
  text: string;
  /** 발화가 확정되었는지(false면 아직 갱신 중인 중간 결과) */
  isFinal: boolean;
}

/** 마이크/인식 오류 유형 — 호출부가 사용자 메시지로 매핑한다. */
export type SttErrorCode =
  | 'not-allowed' // 마이크 권한 거부
  | 'no-speech' // 음성 미감지
  | 'audio-capture' // 마이크 장치 없음
  | 'aborted' // 사용자가 중단(정상)
  | 'network'
  | 'unknown';

export interface SttHandlers {
  /** 인식 텍스트가 갱신될 때(중간 결과 포함) 호출 */
  onTranscript(result: SttTranscript): void;
  /** 오류 발생(권한 거부 등) */
  onError(code: SttErrorCode): void;
  /** 인식 종료(정상 종료·중단 공통) */
  onEnd(): void;
}

/** 진행 중인 인식 세션 핸들. */
export interface SttSession {
  /** 인식을 멈춘다(멱등). */
  stop(): void;
}

/**
 * 음성 → 텍스트 변환 포트. 인식을 시작하면 {@link SttSession} 핸들을 돌려주고,
 * 인식 텍스트는 {@link SttHandlers.onTranscript}로 흘려보낸다.
 */
export interface SttProvider {
  start(handlers: SttHandlers): SttSession;
}
