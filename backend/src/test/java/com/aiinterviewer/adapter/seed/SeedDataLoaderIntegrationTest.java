package com.aiinterviewer.adapter.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.domain.category.Category;
import com.aiinterviewer.domain.category.CategoryPhase;
import com.aiinterviewer.domain.category.CategoryRepository;
import com.aiinterviewer.domain.question.QuestionRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * SeedDataLoader 통합 테스트 — 컨텍스트 기동 시 classpath의 seed/*.json 9개가 모두 적재되는지 검증.
 *
 * <p>기대값은 커밋된 seed 콘텐츠를 추적하는 카나리다. seed가 바뀌면 이 값을 함께 갱신한다.
 * (A안 9개 카테고리, 총 193문항 — 결정사항 D24 제너릭 로더가 파일명→slug, category→이름,
 * phase→MVP 규칙을 새 파일에도 그대로 적용하는지 확인)
 */
@SpringBootTest
class SeedDataLoaderIntegrationTest {

    /** slug, 표시 이름, 문항 수 (합계 193) */
    private record ExpectedCategory(String slug, String name, int questionCount) {
    }

    private static final List<ExpectedCategory> EXPECTED = List.of(
            new ExpectedCategory("os", "운영체제", 44),
            new ExpectedCategory("data-structure", "자료구조", 31),
            new ExpectedCategory("database", "데이터베이스", 24),
            new ExpectedCategory("network", "네트워크", 22),
            new ExpectedCategory("algorithm", "알고리즘", 19),
            new ExpectedCategory("software-engineering", "소프트웨어공학", 15),
            new ExpectedCategory("computer-architecture", "컴퓨터구조", 14),
            new ExpectedCategory("design-pattern", "디자인패턴", 14),
            new ExpectedCategory("common-sense", "개발상식", 10));

    private static final int EXPECTED_TOTAL_QUESTIONS = 193;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Test
    @DisplayName("seed 9개 파일이 카테고리 9개·질문 193건으로 적재된다")
    void loadsAllSeedFiles() {
        assertSoftly(softly -> {
            softly.assertThat(categoryRepository.count()).as("카테고리 수").isEqualTo(9);
            softly.assertThat(questionRepository.count()).as("총 질문 수").isEqualTo(EXPECTED_TOTAL_QUESTIONS);
        });
    }

    @Test
    @DisplayName("각 파일이 파일명→slug, category→이름, phase→MVP, 문항 수 규칙대로 적재된다")
    void loadsEachCategoryByRule() {
        assertSoftly(softly -> {
            for (ExpectedCategory expected : EXPECTED) {
                Category category = categoryRepository.findBySlug(expected.slug()).orElse(null);
                softly.assertThat(category).as("slug=%s 카테고리 존재", expected.slug()).isNotNull();
                if (category == null) {
                    continue;
                }
                softly.assertThat(category.getName())
                        .as("slug=%s 이름", expected.slug()).isEqualTo(expected.name());
                softly.assertThat(category.getPhase())
                        .as("slug=%s phase", expected.slug()).isEqualTo(CategoryPhase.MVP);
                softly.assertThat(questionRepository.findByCategoryId(category.getId()))
                        .as("slug=%s 문항 수", expected.slug()).hasSize(expected.questionCount());
            }
        });
    }

    @Test
    @DisplayName("오프닝(첫 질문) 후보가 카테고리마다 하나 이상 있다")
    void everyCategoryHasOpeningQuestion() {
        assertSoftly(softly -> {
            for (ExpectedCategory expected : EXPECTED) {
                Category category = categoryRepository.findBySlug(expected.slug()).orElseThrow();
                softly.assertThat(questionRepository.findByCategoryIdAndOpeningTrue(category.getId()))
                        .as("slug=%s 오프닝 후보", expected.slug())
                        .isNotEmpty();
            }
        });
    }
}
