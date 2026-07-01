package com.aiinterviewer.llm;

/** LLM 연동 관련 오류의 상위 타입. */
public abstract class LlmException extends RuntimeException {

    protected LlmException(String message) {
        super(message);
    }

    protected LlmException(String message, Throwable cause) {
        super(message, cause);
    }
}
