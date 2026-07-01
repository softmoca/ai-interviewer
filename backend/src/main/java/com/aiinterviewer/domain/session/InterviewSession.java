package com.aiinterviewer.domain.session;

import com.aiinterviewer.common.BaseTimeEntity;
import com.aiinterviewer.common.DomainGuard;
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

    private InterviewSession(User user, Set<Long> categoryIds, boolean randomAll,
                             Integer questionCount, Integer difficulty, LocalDateTime startedAt) {
        this.user = DomainGuard.requireNotNull(user, "user");
        this.randomAll = randomAll;
        this.categoryIds = categoryIds == null ? new HashSet<>() : new HashSet<>(categoryIds);
        if (!randomAll && this.categoryIds.isEmpty()) {
            throw new IllegalArgumentException("카테고리를 하나 이상 선택하거나 전체 랜덤이어야 합니다.");
        }
        if (questionCount != null && questionCount <= 0) {
            throw new IllegalArgumentException("질문 수는 1 이상이어야 합니다: " + questionCount);
        }
        if (difficulty != null) {
            DomainGuard.requireInRange(difficulty, 1, 3, "difficulty");
        }
        this.questionCount = questionCount;
        this.difficulty = difficulty;
        this.status = SessionStatus.IN_PROGRESS;
        this.startedAt = DomainGuard.requireNotNull(startedAt, "startedAt");
    }

    /**
     * 세션을 시작한다(설정 검증 포함). 전체 랜덤이 아니면 카테고리를 하나 이상 선택해야 하고,
     * 난이도는 지정 시 1~3, 질문 수는 지정 시 1 이상이어야 한다. 시작 상태는 진행 중이다.
     */
    public static InterviewSession start(User user, Set<Long> categoryIds, boolean randomAll,
                                         Integer questionCount, Integer difficulty,
                                         LocalDateTime startedAt) {
        return new InterviewSession(user, categoryIds, randomAll, questionCount, difficulty, startedAt);
    }

    // --- 도메인 행위: 상태 전이 규칙은 세션이 소유한다 (domain-design.md §3, AP-1/AP-2 방지) ---

    /** 세션을 정상 종료한다. 진행 중일 때만 허용한다(상태 전이 불변식). */
    public void complete(LocalDateTime endedAt) {
        finish(SessionStatus.COMPLETED, endedAt);
    }

    /** 세션을 중단 처리한다. 진행 중일 때만 허용한다. */
    public void abandon(LocalDateTime endedAt) {
        finish(SessionStatus.ABANDONED, endedAt);
    }

    public boolean isInProgress() {
        return this.status == SessionStatus.IN_PROGRESS;
    }

    private void finish(SessionStatus target, LocalDateTime endedAt) {
        if (!isInProgress()) {
            throw new IllegalStateException("진행 중인 세션만 종료할 수 있습니다. 현재 상태=" + this.status);
        }
        this.status = target;
        this.endedAt = endedAt;
    }
}
