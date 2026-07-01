# CLAUDE.md — 프로젝트 작업 지침

이 파일은 Claude Code가 매 세션 참고하는 프로젝트 규칙/맥락이다.

## 프로젝트 개요
- **이름**: AI 면접관 (CS 기술 면접 준비 서비스)
- **목표**: CS 기술 면접 연습 도구 + 취업 포트폴리오
- **핵심**: 질문 DB에서 첫 질문을 꺼내고, 사용자 답변을 LLM에 넘겨 카테고리 질문 풀을
  참고자료로 주입해 **유연한 꼬리질문**을 생성한다(패턴 B). 세션 종료 후 평가 리포트 제공.

## 기술 스택
- 백엔드: **Spring Boot** (Gradle), Spring Data JPA, Spring Security + JWT
- 프론트엔드: **React** (Vite 권장)
- DB: **H2**(개발 초기) → **로컬 Docker PostgreSQL**(주력) → **AWS RDS**(배포)
- LLM: 외부 LLM API (꼬리질문 생성 + 평가). 응답은 JSON으로 받아 파싱.

## 폴더 구조
```
backend/    # Spring Boot
frontend/   # React
docs/       # 기획서, 아키텍처, 프롬프트설계, 결정사항 (설계 기준)
seed/       # 카테고리별 질문 데이터 (os.json ...)
```

## 반드시 지킬 규칙
1. **설계 기준은 `docs/`를 따른다.** 특히 `docs/결정사항.md`(ADR)의 결정을 존중한다.
2. **기능을 추가/변경하면 관련 `docs/` 문서도 함께 갱신한다.** 새 결정은 결정사항.md에 추가.
3. **비밀정보(LLM API 키 등)는 절대 커밋하지 않는다.** `.env` 사용, `.gitignore`로 제외.
4. 카테고리는 코드에 하드코딩하지 않고 **데이터(테이블)로 관리**한다(확장성).
5. LLM 응답은 항상 **구조화(JSON)**로 받아 백엔드에서 검증 후 저장한다.
6. 평가 점수는 **5점 척도**로 단순화한다.

## 질문 데이터 (seed)
- `seed/*.json` 형식: category / topic / content / difficulty(1~3) / keywords /
  model_answer / source_url / is_opening
- 현재 `seed/os.json`(운영체제) 완료. 남은 A안 카테고리: 자료구조, 알고리즘, 네트워크,
  데이터베이스, 디자인패턴, 컴퓨터구조, 소프트웨어공학, 개발상식.
- 질문 추출 기준은 `docs/결정사항.md` D17 참고.

## 개발 로드맵 (요약)
- M1: 스키마 확정 + 질문 seed 수집
- M2: 백엔드 뼈대(인증/세션/LLM 연동) + 텍스트 면접 1회 완주
- M3: 평가 리포트 + 프론트 연결 (MVP 완성)
- M4: 음성 입력(STT), B안 카테고리(Java/Web/Spring), 소셜 로그인
