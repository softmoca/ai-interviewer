package com.aiinterviewer.domain.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 질문 카테고리. 코드 하드코딩이 아니라 데이터로 관리한다(결정사항 D13).
 * 예: 운영체제(os), 자료구조(data-structure).
 */
@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 카테고리명 (예: 운영체제) */
    @Column(nullable = false, unique = true)
    private String name;

    /** URL/식별용 슬러그 (예: os) — seed 파일명과 대응 */
    @Column(nullable = false, unique = true)
    private String slug;

    /** MVP(A안) / EXPANSION(B안) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryPhase phase;

    /** 목록 노출 순서 */
    @Column(nullable = false)
    private int sortOrder;
}
