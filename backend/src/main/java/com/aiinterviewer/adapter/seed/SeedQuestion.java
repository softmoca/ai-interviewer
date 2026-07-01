package com.aiinterviewer.adapter.seed;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * seed/*.json 한 항목의 파싱 DTO(로더 전용 · 도메인 아님).
 * JSON 직렬화 어노테이션은 이 어댑터 DTO에만 두고 도메인에는 두지 않는다(code-quality §1.2).
 * 필드는 seed 스키마(결정사항 D17)와 정합.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record SeedQuestion(
        String category,
        String topic,
        String content,
        int difficulty,
        List<String> keywords,
        @JsonProperty("model_answer") String modelAnswer,
        @JsonProperty("source_url") String sourceUrl,
        @JsonProperty("is_opening") boolean opening
) {
}
