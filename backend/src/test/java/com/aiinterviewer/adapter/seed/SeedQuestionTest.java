package com.aiinterviewer.adapter.seed;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * SeedQuestion 파싱 단위 테스트 — seed JSON 스키마 매핑/방어 검증(Spring 없이).
 * snake_case 매핑(model_answer/source_url/is_opening)과 알 수 없는/선택 필드 방어.
 */
class SeedQuestionTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("snake_case 필드를 매핑하고 알 수 없는 필드는 무시한다")
    void mapsSnakeCaseAndIgnoresUnknown() throws Exception {
        String json = """
                {
                  "category": "운영체제",
                  "topic": "개요",
                  "content": "운영체제의 역할은?",
                  "difficulty": 2,
                  "keywords": ["자원 관리", "스케줄링"],
                  "model_answer": "중재자 역할...",
                  "source_url": "http://example.com",
                  "is_opening": true,
                  "unknown_field": "무시되어야 함"
                }
                """;

        SeedQuestion parsed = objectMapper.readValue(json, SeedQuestion.class);

        assertSoftly(softly -> {
            softly.assertThat(parsed.category()).isEqualTo("운영체제");
            softly.assertThat(parsed.topic()).isEqualTo("개요");
            softly.assertThat(parsed.difficulty()).isEqualTo(2);
            softly.assertThat(parsed.keywords()).containsExactly("자원 관리", "스케줄링");
            softly.assertThat(parsed.modelAnswer()).isEqualTo("중재자 역할...");
            softly.assertThat(parsed.sourceUrl()).isEqualTo("http://example.com");
            softly.assertThat(parsed.opening()).isTrue();
        });
    }

    @Test
    @DisplayName("선택 필드(model_answer/source_url/is_opening)가 없으면 기본값으로 방어된다")
    void toleratesMissingOptionalFields() throws Exception {
        String json = """
                {"category":"c","topic":"t","content":"q","difficulty":1,"keywords":[]}
                """;

        SeedQuestion parsed = objectMapper.readValue(json, SeedQuestion.class);

        assertSoftly(softly -> {
            softly.assertThat(parsed.modelAnswer()).isNull();
            softly.assertThat(parsed.sourceUrl()).isNull();
            softly.assertThat(parsed.opening()).isFalse();
        });
    }
}
