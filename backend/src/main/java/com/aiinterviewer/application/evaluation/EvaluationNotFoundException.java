package com.aiinterviewer.application.evaluation;

/** 아직 평가되지 않은 세션의 리포트를 조회하려 한 경우. */
public class EvaluationNotFoundException extends RuntimeException {

    public EvaluationNotFoundException(Long sessionId) {
        super("아직 평가 리포트가 없습니다: " + sessionId);
    }
}
