# AI 면접관 (AI Interviewer)

CS 기술 면접을 실전처럼 연습할 수 있는 **AI 면접관 웹 서비스**입니다.
단순 예상 질문 나열이 아니라, 사용자의 답변을 읽고 **꼬리질문**을 이어가며
세션 종료 후 **평가 리포트**를 제공하는 것을 목표로 합니다.

## ✨ 핵심 기능

- 카테고리 선택(자료구조/알고리즘/운영체제/네트워크/DB/디자인패턴 등) — 단일·다중·랜덤
- 질문 DB 기반 첫 질문 + **LLM 기반 유연한 꼬리질문**
- 답변 평가 리포트: 개념별 정확성/깊이 점수, 놓친 키워드, 모범답안
- 회원가입·로그인 및 사용자별 면접 기록 저장

## 🧩 동작 컨셉 (패턴 B)

첫 질문은 카테고리별 질문 DB에서 꺼내고, 꼬리질문은 사용자 답변을 LLM에 넘겨
**해당 카테고리 질문 풀을 참고자료로 주입**한 뒤 생성합니다. 통제된 CS 범위 안에서
답변 맥락에 맞춰 유연하게 파고드는 방식입니다.

## 🛠 기술 스택

| 영역 | 기술 |
| --- | --- |
| Backend | Spring Boot, Spring Data JPA, Spring Security + JWT |
| Frontend | React (Vite) |
| Database | H2 → PostgreSQL(Docker) → AWS RDS |
| LLM | 외부 LLM API |

## 📂 프로젝트 구조

```
ai-interviewer/
├── backend/    # Spring Boot 서버
├── frontend/   # React 클라이언트
├── docs/       # 기획서, 아키텍처, 프롬프트 설계, 결정사항(ADR)
└── seed/       # 카테고리별 질문 데이터 (JSON)
```

## 🚀 실행 (백엔드)

사전 요구: **JDK 21**. (Gradle은 포함된 Wrapper 사용)

**1) LLM 키 설정 (선택)** — 꼬리질문 기능을 쓰려면 `backend/.env` 파일을 만들고 키를 넣습니다:

```dotenv
GEMINI_API_KEY=발급받은_키
```

- `.env`는 커밋되지 않습니다(`.gitignore`). 예시는 [`backend/.env.example`](./backend/.env.example).
- **키가 없어도 앱은 정상 실행**됩니다. 이 경우 꼬리질문 호출만 `503(LLM_NOT_CONFIGURED)`로 비활성됩니다.
- 키 발급: Google AI Studio (Gemini API, 무료 티어). IntelliJ로 실행해도 `backend/.env`가 자동 로딩됩니다(spring-dotenv).

**2) 서버 실행**

```bash
cd backend
./gradlew bootRun
```

- 헬스체크: `GET http://localhost:8080/api/health`
- H2 콘솔: `http://localhost:8080/h2-console`
- 주요 API: `/api/auth/{signup,login,me}`, `/api/sessions` (설계는 [아키텍처](./docs/아키텍처.md) 참고)

**3) 테스트**

```bash
cd backend && ./gradlew test
```

- 실제 Gemini를 호출하는 테스트는 **OS 환경변수로 키가 있을 때만** 실행됩니다(없으면 skip):
  ```bash
  GEMINI_API_KEY=... ./gradlew test --tests '*GeminiLlmClientLiveTest'
  ```

## 🖥 실행 (프론트엔드)

사전 요구: **Node 18+**. 백엔드(8080)를 함께 띄워야 API가 동작합니다.

```bash
cd frontend
npm install
npm run dev      # http://localhost:5173
```

- 개발 시 `/api` 요청은 **Vite 프록시**가 백엔드(8080)로 전달합니다(CORS 불필요).
- 백엔드를 직접 호출하려면 `frontend/.env`에 `VITE_API_BASE_URL=http://localhost:8080/api`
  (백엔드 CORS가 `localhost:5173` 허용). 예시는 [`frontend/.env.example`](./frontend/.env.example).
- 빌드/테스트: `npm run build`(타입체크+번들), `npm run test`(vitest 스모크).

## 📖 문서

설계와 의사결정은 [`docs/`](./docs) 에 정리되어 있습니다.

- [기획서](./docs/기획서.md) · [아키텍처](./docs/아키텍처.md) · [프롬프트 설계](./docs/프롬프트-설계.md)
- [코드 품질 기준](./docs/code-quality-standards.md) · [도메인 설계](./docs/domain-design.md) · [테스트 전략](./docs/test-strategy.md)
- [결정사항 (ADR)](./docs/결정사항.md)

## 🚧 개발 상태

로드맵은 [기획서](./docs/기획서.md)를 참고하세요.

- [x] 기획·설계 문서화 + 질문 데이터 수집 (A안 9개 카테고리, 193문항)
- [x] 백엔드 인증 (Spring Security + JWT)
- [x] 세션 API + seed 제너릭 적재
- [x] LLM 꼬리질문 (Gemini)
- [x] 세션 평가 리포트 (개념별 5점 + 모범답안 + 총평) → **텍스트 면접 1회 완주**
- [x] 프론트엔드 연결 — **MVP 완성**: 인증 · 면접 진행(설정→질문→답변→꼬리질문→종료) · 평가 리포트
- [x] 사용자별 세션 목록/다시보기 (지난 면접 조회)
- [ ] 음성 입력, 카테고리 확장, 소셜 로그인
