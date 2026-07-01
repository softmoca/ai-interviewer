package com.aiinterviewer.application.session;

/** 존재하지 않는 카테고리 슬러그로 세션을 시작하려 한 경우. */
public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(String slug) {
        super("존재하지 않는 카테고리입니다: " + slug);
    }
}
