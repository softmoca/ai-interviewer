package com.aiinterviewer.domain.question;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.domain.category.Category;
import com.aiinterviewer.domain.category.CategoryPhase;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Question 생성 규칙 단위 테스트 (생성 경로 = seed 로더, test-strategy.md §1/D23). */
class QuestionTest {

    private static final Category CATEGORY = Category.of("운영체제", "os", CategoryPhase.MVP, 0);

    @Test
    @DisplayName("유효한 값으로 질문을 생성하고 keywords는 방어적으로 복사한다")
    void createsQuestion() {
        List<String> keywords = List.of("프로세스", "스레드");
        Question question = Question.of(CATEGORY, "프로세스와 스레드", "차이를 설명하세요.", 2,
                keywords, "모범답안", "http://src", true);

        assertSoftly(softly -> {
            softly.assertThat(question.getCategory()).isSameAs(CATEGORY);
            softly.assertThat(question.getTopic()).isEqualTo("프로세스와 스레드");
            softly.assertThat(question.getDifficulty()).isEqualTo(2);
            softly.assertThat(question.isOpening()).isTrue();
            softly.assertThat(question.getKeywords()).containsExactly("프로세스", "스레드");
        });
    }

    @Test
    @DisplayName("keywords가 null이면 빈 리스트로 방어된다")
    void nullKeywordsBecomesEmpty() {
        Question question = Question.of(CATEGORY, "t", "c", 1, null, null, null, false);

        assertThat(question.getKeywords()).isEmpty();
    }

    @Nested
    @DisplayName("생성 거부")
    class Rejects {

        @Test
        @DisplayName("난이도가 1~3을 벗어나면 예외")
        void rejectsDifficultyOutOfRange() {
            assertThatThrownBy(() -> Question.of(CATEGORY, "t", "c", 0, null, null, null, false))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Question.of(CATEGORY, "t", "c", 4, null, null, null, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("카테고리 null 또는 topic/content 공백이면 예외")
        void rejectsMissingRequired() {
            assertThatThrownBy(() -> Question.of(null, "t", "c", 1, null, null, null, false))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Question.of(CATEGORY, " ", "c", 1, null, null, null, false))
                    .isInstanceOf(IllegalArgumentException.class);
            assertThatThrownBy(() -> Question.of(CATEGORY, "t", " ", 1, null, null, null, false))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("경계 난이도 1과 3은 허용")
        void allowsBoundaryDifficulty() {
            assertThatCode(() -> Question.of(CATEGORY, "t", "c", 1, null, null, null, false))
                    .doesNotThrowAnyException();
            assertThatCode(() -> Question.of(CATEGORY, "t", "c", 3, null, null, null, false))
                    .doesNotThrowAnyException();
        }
    }
}
