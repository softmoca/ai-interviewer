package com.aiinterviewer.domain.question;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    /** 카테고리의 질문 풀 조회 — 꼬리질문 참고자료 주입에 사용 */
    List<Question> findByCategoryId(Long categoryId);

    /** 카테고리의 오프닝(첫 질문) 후보 조회 */
    List<Question> findByCategoryIdAndOpeningTrue(Long categoryId);
}
