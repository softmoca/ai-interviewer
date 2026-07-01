package com.aiinterviewer.llm;

/** LLM 호출 실패(네트워크/HTTP 오류, 빈 응답, JSON 파싱 실패 등). */
public class LlmCallException extends LlmException {

    public LlmCallException(String message, Throwable cause) {
        super(message, cause);
    }

    public LlmCallException(String message) {
        super(message);
    }
}
