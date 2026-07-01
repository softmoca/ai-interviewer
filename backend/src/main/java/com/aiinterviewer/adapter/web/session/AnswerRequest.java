package com.aiinterviewer.adapter.web.session;

import jakarta.validation.constraints.NotBlank;

/** 답변 제출 요청 바디. */
public record AnswerRequest(
        @NotBlank String content
) {
}
