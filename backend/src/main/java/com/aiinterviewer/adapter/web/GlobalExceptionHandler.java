package com.aiinterviewer.adapter.web;

import com.aiinterviewer.application.auth.AuthenticationFailedException;
import com.aiinterviewer.application.auth.DuplicateEmailException;
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
