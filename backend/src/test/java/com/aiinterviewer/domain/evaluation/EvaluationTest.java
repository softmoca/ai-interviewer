package com.aiinterviewer.domain.evaluation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Evaluation 점수 불변식 단위 테스트 — 5점 척도(1~5)를 도메인 경계에서 강제하는지 검증
 * (D10, AP-9 방지 / test-strategy.md §1). 세션 연관은 검증 대상이 아니라 null로 둔다.
 */
class EvaluationTest {

    @Test
    @DisplayName("유효한 점수로 생성하면 값이 그대로 담긴다")
    void createsWithValidScores() {
        Evaluation evaluation = Evaluation.of(
                null, "프로세스와 스레드", 4, 3, List.of("PCB", "context switching"), "모범답안", "총평");

        assertSoftly(softly -> {
            softly.assertThat(evaluation.getConcept()).isEqualTo("프로세스와 스레드");
            softly.assertThat(evaluation.getAccuracyScore()).isEqualTo(4);
            softly.assertThat(evaluation.getDepthScore()).isEqualTo(3);
            softly.assertThat(evaluation.getMissedKeywords()).containsExactly("PCB", "context switching");
        });
    }

    @Test
    @DisplayName("경계값 1과 5는 허용된다")
    void allowsBoundaryScores() {
        assertThatCode(() -> Evaluation.of(null, "c", 1, 5, null, null, null))
                .doesNotThrowAnyException();
        assertThatCode(() -> Evaluation.of(null, "c", 5, 1, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("missedKeywords가 null이면 빈 리스트로 방어된다")
    void nullMissedKeywordsBecomesEmpty() {
        Evaluation evaluation = Evaluation.of(null, "c", 3, 3, null, null, null);

        assertThat(evaluation.getMissedKeywords()).isEmpty();
    }

    @Nested
    @DisplayName("점수 범위(1~5) 위반은 생성 거부")
    class RejectsOutOfRange {

        @Test
        @DisplayName("accuracy가 범위를 벗어나면 예외")
        void rejectsAccuracyOutOfRange() {
            assertThatThrownBy(() -> Evaluation.of(null, "c", 0, 3, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Evaluation.of(null, "c", 6, 3, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("depth가 범위를 벗어나면 예외")
        void rejectsDepthOutOfRange() {
            assertThatThrownBy(() -> Evaluation.of(null, "c", 3, 0, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Evaluation.of(null, "c", 3, 6, null, null, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
