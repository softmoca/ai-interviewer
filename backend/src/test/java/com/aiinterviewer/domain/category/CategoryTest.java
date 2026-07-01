package com.aiinterviewer.domain.category;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Category 생성 규칙 단위 테스트 (생성 경로 = seed 로더, test-strategy.md §1/D23). */
class CategoryTest {

    @Test
    @DisplayName("유효한 값으로 카테고리를 생성한다")
    void createsCategory() {
        Category category = Category.of("운영체제", "os", CategoryPhase.MVP, 1);

        assertSoftly(softly -> {
            softly.assertThat(category.getName()).isEqualTo("운영체제");
            softly.assertThat(category.getSlug()).isEqualTo("os");
            softly.assertThat(category.getPhase()).isEqualTo(CategoryPhase.MVP);
            softly.assertThat(category.getSortOrder()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("이름/슬러그가 비거나 phase가 null이면 거부한다")
    void rejectsInvalid() {
        assertThatThrownBy(() -> Category.of(" ", "os", CategoryPhase.MVP, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Category.of("운영체제", " ", CategoryPhase.MVP, 0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> Category.of("운영체제", "os", null, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
