package com.aiinterviewer.adapter.web.session;

import com.aiinterviewer.application.session.AnswerResult;
import com.aiinterviewer.application.session.SessionDetailResult;
import com.aiinterviewer.application.session.SessionService;
import com.aiinterviewer.application.session.SessionStatusResult;
import com.aiinterviewer.application.session.StartSessionCommand;
import com.aiinterviewer.application.session.StartSessionResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 세션 API(웹 어댑터). 인증 필요(SecurityConfig의 anyRequest authenticated). HTTP↔애플리케이션
 * 번역만 담당하고 업무 판단은 하지 않는다(AP-1 방지). 인증 주체(userId)는 JWT 필터가 설정.
 *
 * <ul>
 *   <li>POST /api/sessions — 세션 시작 + 첫 질문 서빙</li>
 *   <li>POST /api/sessions/{id}/answers — 답변 기록</li>
 *   <li>POST /api/sessions/{id}/complete — 세션 종료</li>
 *   <li>GET  /api/sessions/{id} — 세션 상세(대화 이력)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StartSessionResult start(@AuthenticationPrincipal Long userId,
                                    @RequestBody StartSessionRequest request) {
        StartSessionCommand command = new StartSessionCommand(request.categorySlugs(),
                request.randomAll(), request.questionCount(), request.difficulty());
        return sessionService.startSession(userId, command);
    }

    @PostMapping("/{sessionId}/answers")
    @ResponseStatus(HttpStatus.CREATED)
    public AnswerResult answer(@AuthenticationPrincipal Long userId,
                               @PathVariable Long sessionId,
                               @Valid @RequestBody AnswerRequest request) {
        return sessionService.submitAnswer(userId, sessionId, request.content());
    }

    @PostMapping("/{sessionId}/complete")
    public SessionStatusResult complete(@AuthenticationPrincipal Long userId,
                                        @PathVariable Long sessionId) {
        return sessionService.completeSession(userId, sessionId);
    }

    @GetMapping("/{sessionId}")
    public SessionDetailResult get(@AuthenticationPrincipal Long userId,
                                   @PathVariable Long sessionId) {
        return sessionService.getSession(userId, sessionId);
    }
}
