package com.aiinterviewer.domain.question;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** 카테고리의 질문 풀 조회 — 꼬리질문 참고자료 주입에 사용 */
    List<Question> findByCategoryId(Long categoryId);

    /** 카테고리의 오프닝(첫 질문) 후보 조회 */
    List<Question> findByCategoryIdAndOpeningTrue(Long categoryId);

    /** 전체 오프닝 후보 조회 — 전체 랜덤 세션의 첫 질문 선택에 사용 */
    List<Question> findByOpeningTrue();

    /** 선택 카테고리들의 오프닝 후보 조회 */
    List<Question> findByCategoryIdInAndOpeningTrue(Collection<Long> categoryIds);
}
