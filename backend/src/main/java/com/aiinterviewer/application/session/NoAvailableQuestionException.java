package com.aiinterviewer.application.session;

/** 선택한 조건(카테고리/난이도)에 맞는 오프닝 질문이 하나도 없는 경우. */
public class NoAvailableQuestionException extends RuntimeException {

    public NoAvailableQuestionException() {
        super("조건에 맞는 첫 질문이 없습니다.");
    }
}
