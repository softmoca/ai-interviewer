package com.aiinterviewer.domain.session;

import com.aiinterviewer.common.BaseTimeEntity;
import com.aiinterviewer.domain.user.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 한 번의 면접 세션. 세션 설정(선택 카테고리/질문 수/난이도)과 진행 상태를 담는다
 * (docs/기획서.md 5.1 세션 설정, docs/아키텍처.md interview_session).
 */
@Getter
@Entity
@Table(name = "interview_session")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InterviewSession extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 선택한 카테고리 id 목록. 전체 랜덤이면 {@code randomAll=true}로 비워둘 수 있음. */
    @ElementCollection
    @CollectionTable(name = "session_category", joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "category_id")
    private Set<Long> categoryIds = new HashSet<>();

    /** 전체 카테고리 랜덤 진행 여부 */
    @Column(nullable = false)
    private boolean randomAll;

    /** 목표 질문 수 */
    private Integer questionCount;

    /** 세션 난이도 (1~3, nullable = 혼합) */
    private Integer difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.IN_PROGRESS;

    private LocalDateTime startedAt;

    private LocalDateTime endedAt;
}
