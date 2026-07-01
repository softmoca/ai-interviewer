package com.aiinterviewer.llm;

/**
 * LLM이 설정되지 않아(예: API 키 없음) 호출할 수 없는 경우.
 * 앱은 정상 기동하되, LLM 기능만 이 예외로 명확히 비활성됨을 알린다(결정사항 D26).
 */
public class LlmNotConfiguredException extends LlmException {

    public LlmNotConfiguredException(String message) {
        super(message);
    }
}
