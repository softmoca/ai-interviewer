package com.aiinterviewer.domain.question;

import com.aiinterviewer.domain.category.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문 풀의 단일 질문. 첫 질문(오프닝)이자 꼬리질문 생성 시 LLM에 주입하는 참고자료다
 * (패턴 B — docs/프롬프트-설계.md). seed 파일 스키마와 정합(결정사항 D17).
 */
@Getter
@Entity
@Table(name = "question")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    /** 카테고리 내 세부 주제 (예: 세마포어와 뮤텍스) — 주제 단위 묶음 관리 */
    @Column(nullable = false)
    private String topic;

    /** 질문 본문 */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /** 난이도 1~3 (1=기본 정의, 2=비교·이유, 3=세부 메커니즘) */
    @Column(nullable = false)
    private int difficulty;

    /** 핵심 키워드 — 평가 시 '놓친 키워드' 판정 등에 활용 */
    @ElementCollection
    @CollectionTable(name = "question_keyword", joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "keyword")
    private List<String> keywords = new ArrayList<>();

    /** 모범답안 (3~4문장, nullable) */
    @Column(columnDefinition = "TEXT")
    private String modelAnswer;

    /** 출처 URL (추적용) */
    private String sourceUrl;

    /** 첫 질문(오프닝) 후보 여부 */
    @Column(nullable = false)
    private boolean opening;
}
