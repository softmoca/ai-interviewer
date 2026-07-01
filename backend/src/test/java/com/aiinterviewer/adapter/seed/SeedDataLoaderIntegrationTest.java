package com.aiinterviewer.adapter.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.aiinterviewer.domain.category.Category;
import com.aiinterviewer.domain.category.CategoryRepository;
import com.aiinterviewer.domain.question.QuestionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * SeedDataLoader 통합 테스트 — 컨텍스트 기동 시 classpath seed/os.json이 실제로 적재되는지 검증.
 * (전체 질문 수가 아니라 os 카테고리 범위로 단언 → 이후 다른 seed 추가에 깨지지 않음)
 */
@SpringBootTest
class SeedDataLoaderIntegrationTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Test
    @DisplayName("os.json이 카테고리(운영체제/os)와 질문 44건(오프닝 21건)으로 적재된다")
    void loadsOsSeed() {
        Category os = categoryRepository.findBySlug("os").orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(os.getName()).isEqualTo("운영체제");
            softly.assertThat(questionRepository.findByCategoryId(os.getId())).hasSize(44);
            softly.assertThat(questionRepository.findByCategoryIdAndOpeningTrue(os.getId())).hasSize(21);
        });
    }
}
