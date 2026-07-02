package com.aiinterviewer.application.session;

import com.aiinterviewer.domain.question.Question;
import com.aiinterviewer.domain.session.QaLog;
import com.aiinterviewer.domain.session.QaRole;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 꼬리질문 프롬프트 조립(애플리케이션 관심사 — domain-design.md §3). 면접관 시스템 프롬프트
 * (docs/프롬프트-설계.md §2)에 해당 카테고리 질문 풀(참고자료)과 대화 이력을 주입한다(패턴 B).
 *
 * <p>응답은 구조화 JSON으로 받도록 지시한다(규칙 5). LLM 프로바이더에 독립적인 순수 문자열 조립.
 */
@Component
public class FollowUpPromptFactory {

    public String build(List<Question> questionPool, List<QaLog> transcript) {
        StringBuilder sb = new StringBuilder();
        sb.append("""
                너는 IT 기업의 CS 기술 면접관이다.

                [역할]
                - 지원자의 답변을 듣고, 실제 면접처럼 이해도를 검증하는 꼬리질문을 던진다.
                - 암기가 아니라 개념 이해를 확인하는 것이 목적이다.

                [진행 규칙]
                - 지원자의 답변에서 부정확하거나 얕은 부분을 찾아 그곳을 파고든다.
                - 답변이 충분히 깊고 정확하면, 인접한 개념으로 자연스럽게 확장한다.
                - 정답을 먼저 알려주지 않는다.
                - 아래 [참고 질문 풀]에서 크게 벗어나지 말되, 답변에 명백한 오류·모순이 있으면 그것을 우선 파고든다.

                [톤]
                - 실제 면접관처럼 간결하고 정중하게. 과한 칭찬이나 잡담은 하지 않는다.

                [참고 질문 풀]
                """);
        if (questionPool.isEmpty()) {
            sb.append("- (없음)\n");
        } else {
            for (Question q : questionPool) {
                sb.append("- (").append(q.getTopic()).append(") ").append(q.getContent()).append('\n');
            }
        }

        sb.append("\n[대화 이력]\n");
        for (QaLog log : transcript) {
            String speaker = log.getRole() == QaRole.INTERVIEWER ? "면접관" : "지원자";
            sb.append(speaker).append(": ").append(log.getContent()).append('\n');
        }

        sb.append("""

                이제 지원자의 마지막 답변에 대한 꼬리질문을 1~2개 생성하라.
                반드시 아래 JSON 형식으로만 답하라(다른 텍스트 금지):
                {"follow_up_questions": ["...", "..."], "reason": "질문 의도", "within_pool": true}
                """);
        return sb.toString();
    }
}
