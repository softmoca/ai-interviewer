package com.aiinterviewer.common;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 뼈대 부팅 확인용 헬스 체크 엔드포인트.
 * (실제 도메인 API는 M2에서 category/question/session/evaluation 하위에 추가)
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "ai-interviewer");
    }
}
