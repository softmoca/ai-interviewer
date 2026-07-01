package com.aiinterviewer.common;

/**
 * 도메인 생성 시 반복되는 일반 전제조건(precondition) 검사 모음.
 *
 * <p>여기 담는 것은 "빈값/범위" 같은 <b>일반 검증</b>일 뿐, 특정 업무 규칙이 아니다.
 * 업무 규칙은 각 도메인 객체가 소유한다(AP-5 회피). 위반 시 {@link IllegalArgumentException}.
 */
public final class DomainGuard {

    private DomainGuard() {
    }

    public static String requireNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + "은(는) 필수입니다.");
        }
        return value;
    }

    public static <T> T requireNotNull(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + "은(는) 필수입니다.");
        }
        return value;
    }

    public static int requireInRange(int value, int min, int max, String field) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(
                    "%s은(는) %d~%d 범위여야 합니다: %d".formatted(field, min, max, value));
        }
        return value;
    }
}
