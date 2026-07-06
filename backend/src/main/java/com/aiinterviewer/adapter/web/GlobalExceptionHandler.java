package com.aiinterviewer.adapter.web;

import com.aiinterviewer.application.auth.AuthenticationFailedException;
import com.aiinterviewer.application.auth.DuplicateEmailException;
import com.aiinterviewer.application.auth.SocialAuthenticationException;
import com.aiinterviewer.application.session.CategoryNotFoundException;
import com.aiinterviewer.application.session.NoAvailableQuestionException;
import com.aiinterviewer.application.session.SessionAccessDeniedException;
import com.aiinterviewer.application.session.SessionNotFoundException;
import com.aiinterviewer.application.evaluation.EvaluationNotFoundException;
import com.aiinterviewer.application.evaluation.SessionNotCompletedException;
import com.aiinterviewer.application.session.SessionNotInProgressException;
import com.aiinterviewer.llm.LlmCallException;
import com.aiinterviewer.llm.LlmNotConfiguredException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외 → HTTP 응답 번역(웹 어댑터). 도메인/애플리케이션 예외를 상태코드로 매핑해
 * 일관된 에러 바디를 반환한다(검증 위치 일관성 — AP-6 방지).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 표준 에러 응답 바디. */
    public record ErrorResponse(String code, String message) {
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_EMAIL", e.getMessage()));
    }

    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailed(AuthenticationFailedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("AUTHENTICATION_FAILED", e.getMessage()));
    }

    /** 소셜 로그인 실패(토큰 무효/이메일 미검증/미지원 프로바이더) → 401 (D38). */
    @ExceptionHandler(SocialAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSocialAuthentication(SocialAuthenticationException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse("SOCIAL_AUTHENTICATION_FAILED", e.getMessage()));
    }

    @ExceptionHandler({SessionNotFoundException.class, CategoryNotFoundException.class,
            NoAvailableQuestionException.class, EvaluationNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(SessionAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleSessionAccessDenied(SessionAccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("ACCESS_DENIED", e.getMessage()));
    }

    /** 상태 충돌(진행 중 아님/미완료 세션 평가/세션 재종료 IllegalStateException) → 409. */
    @ExceptionHandler({SessionNotInProgressException.class, SessionNotCompletedException.class,
            IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("CONFLICT", e.getMessage()));
    }

    /** LLM 미설정(키 없음) → 503. 앱은 살아있고 LLM 기능만 비활성임을 알린다(D26). */
    @ExceptionHandler(LlmNotConfiguredException.class)
    public ResponseEntity<ErrorResponse> handleLlmNotConfigured(LlmNotConfiguredException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse("LLM_NOT_CONFIGURED", e.getMessage()));
    }

    /** LLM 호출/파싱 실패 → 502. */
    @ExceptionHandler(LlmCallException.class)
    public ResponseEntity<ErrorResponse> handleLlmCall(LlmCallException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ErrorResponse("LLM_CALL_FAILED", e.getMessage()));
    }

    /** 요청 바디 검증 실패(@Valid). 첫 위반 메시지를 대표로 담는다. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("잘못된 요청입니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST", message));
    }

    /** 도메인 불변식 위반 등 잘못된 인자. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_REQUEST", e.getMessage()));
    }
}
