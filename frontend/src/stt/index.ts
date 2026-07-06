// STT 포트 진입점 — 결정사항 D37.
// 호출부는 이 팩토리만 쓰고 구체 구현(Web Speech)을 알지 못한다. 나중에 프로바이더를
// 교체하려면 여기 한 곳만 바꾸면 된다.

import type { SttProvider } from './types';
import { createWebSpeechProvider } from './webSpeech';

export type { SttProvider, SttHandlers, SttSession, SttTranscript, SttErrorCode } from './types';
export { isSpeechInputSupported } from './webSpeech';

/** 현재 브라우저에 맞는 STT 프로바이더를 반환한다(미지원이면 null → 폴백). */
export function createSttProvider(win: Window = window, lang = 'ko-KR'): SttProvider | null {
  return createWebSpeechProvider(win, lang);
}
