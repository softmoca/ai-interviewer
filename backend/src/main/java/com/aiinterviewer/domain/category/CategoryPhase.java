package com.aiinterviewer.domain.category;

/**
 * 카테고리가 속한 단계. A안/B안 구분 (docs/기획서.md 6번).
 * MVP = 핵심 CS(A안), EXPANSION = 직무 밀착(B안: Java/Web/Spring 등).
 */
public enum CategoryPhase {
    MVP,
    EXPANSION
}
