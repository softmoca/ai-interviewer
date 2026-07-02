package com.aiinterviewer.adapter.web.evaluation;

import com.aiinterviewer.application.evaluation.EvaluationReportResult;
import com.aiinterviewer.application.evaluation.EvaluationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 세션 평가 리포트 API(웹 어댑터). 인증 필요. HTTP↔애플리케이션 번역만 담당.
 *
 * <ul>
 *   <li>POST /api/sessions/{id}/evaluation — 평가 생성(멱등: 이미 있으면 기존 반환)</li>
 *   <li>GET  /api/sessions/{id}/evaluation — 저장된 리포트 조회(없으면 404)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sessions/{sessionId}/evaluation")
public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public EvaluationReportResult evaluate(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long sessionId) {
        return evaluationService.evaluate(userId, sessionId);
    }

    @GetMapping
    public EvaluationReportResult get(@AuthenticationPrincipal Long userId,
                                      @PathVariable Long sessionId) {
        return evaluationService.getReport(userId, sessionId);
    }
}
