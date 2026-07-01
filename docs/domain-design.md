# 도메인 설계 (Domain Design)

> **AI 면접관**의 도메인 모델을 정리한다. 유비쿼터스 언어, Entity/VO/Aggregate 분류,
> 비즈니스 규칙의 소유자, 패키지 구조를 담는다. `docs/아키텍처.md`의 DB 스키마와 정합.
> 코드 판단 기준은 `docs/code-quality-standards.md`를 따른다.

---

## 1. 유비쿼터스 언어 (용어 통일)

> "동일 개념 = 동일 어휘". 코드/문서/대화에서 한 개념을 한 단어로만 부른다.
> 코드 식별자는 영어, 문서 설명은 한국어를 허용한다. 패턴 접미사(Manager/Handler)는 금지.

| 한국어 | 코드(영어) | 정의 |
|--------|-----------|------|
| 카테고리 | `Category` | CS 주제 대분류(운영체제, 자료구조 …). **데이터로 관리**(D13). |
| 주제 | `topic` | 카테고리 내 세부 묶음(예: "세마포어와 뮤텍스"). 질문의 속성. |
| 질문 | `Question` | 질문 풀의 한 문항. 첫 질문이자 꼬리질문 생성 시 LLM 참고자료(패턴 B). |
| 오프닝 | `opening` | 첫 질문 후보 여부. |
| 사용자 | `User` | 서비스 회원. |
| 면접 세션 | `InterviewSession` | 한 번의 면접(설정+진행 상태). 대화의 **경계(루트)**. |
| 문답 로그 | `QaLog` | 세션 안의 질문/답변 한 줄. |
| 꼬리질문 | `followUp` | 사용자 답변에 반응해 LLM이 생성한 후속 질문. |
| 평가 | `Evaluation` | 세션 종료 후 개념별 점수/피드백 한 건. |
| 정확성/깊이 | `accuracy`/`depth` | 평가 점수축. **5점 척도**(D10). |
| 놓친 키워드 | `missedKeywords` | 답변에 있었으면 좋았을 핵심어. |
| 질문 풀 | question pool | 특정 카테고리의 질문 집합. 꼬리질문 프롬프트에 주입되는 참고자료. |

> **금지 동의어**: "인터뷰/면접/세션"을 뒤섞지 말 것 → 세션은 `InterviewSession`.
> "점수/스코어/평점" → `score`(accuracy/depth)로 통일.

---

## 2. Entity / Value Object / Aggregate 분류

분류 원칙:
- **Entity**: 식별성과 생애주기를 가진다(시간에 따라 상태가 변하고, 같은 것인지 추적된다).
- **Value Object**: 값 자체로 동일성이 결정되고 **불변**이며, 도메인 메서드나 형식 검증을 가진다.
- **Aggregate**: 함께 생성·변경·삭제되는 일관성 경계. 외부는 **루트를 통해서만** 접근.

### 2.1 Entity
| Entity | 식별/상태 | 소유 행위(예정 포함) |
|--------|-----------|----------------------|
| `Category` | 참조(마스터) 데이터 | 거의 불변. 확장은 데이터 추가로. |
| `Question` | 질문 풀 항목 | `isOpening()`, 난이도(1~3) 불변식. |
| `User` | 회원 | 가입/인증 관련(비밀번호 해시 보관 등, M2). |
| `InterviewSession` | 진행 상태 변함 | **상태 전이**: `start`→`complete`/`abandon`. **세션 애그리거트 루트.** |
| `QaLog` | 세션에 종속 | 생성 시 role/followUp 일관성 규칙. |
| `Evaluation` | 세션에 종속 | 생성 시 **점수 1~5 불변식**. |

### 2.2 Value Object (도입 후보 — M2에서 구체화)
> 원칙: "도메인 메서드가 가능" 하거나 "의미 있는 형식 검증이 있으면" VO로 승격. 아니면
> 생성자 검증으로 흡수(과설계 금지, Kent Beck 규칙 4).

| 후보 | VO 승격 이유 | 현재 처리 |
|------|-------------|-----------|
| `Score`(accuracy/depth) | 1~5 범위 규칙 보유 → 행위 있음 | **VO 유력**. 현재는 `Evaluation` 생성자 검증으로 시작. |
| `Difficulty` | 1~3 범위 + 의미(기본/비교/메커니즘) | VO 후보. 현재 int + 검증. |
| `Keywords`/`MissedKeywords` | 컬렉션 방어적 노출 | 현재 `List<String>`(ElementCollection). |
| `SessionSetup`(categoryIds+randomAll+questionCount+difficulty) | 세션 설정 묶음 규칙 | VO 후보. 현재 세션 필드. |
| `Email`,`Nickname` | 형식 검증 | M2 인증 구현 시. |

### 2.3 Aggregate
- **`InterviewSession` 애그리거트** = 루트 `InterviewSession` + `QaLog`들(+ 세션당 `Evaluation`들).
  - 일관성 규칙: **대화 로그·평가는 세션 없이 존재하지 않는다.** 세션 상태가 규칙을 통제.
  - 외부(서비스)는 세션을 통해 로그를 추가하고, 세션 종료를 통해 평가를 확정한다.
- `Category`·`Question`·`User`는 **각각 독립 애그리거트**(참조 데이터/회원). 세션은 이들을
  **id 참조**로만 느슨하게 연결한다(애그리거트 간 결합 최소화).

---

## 3. 비즈니스 규칙의 소유자 (누가 판단하는가)

> Tell, Don't Ask. 단일 객체로 끝나는 판단은 그 객체의 메서드에, 애그리거트 경계를 넘는
> 조율은 루트/애플리케이션에.

| 규칙 | 소유자 | 비고 |
|------|--------|------|
| 세션 상태 전이(진행→완료/중단) | `InterviewSession.complete()/abandon()` | 진행중일 때만 전이 허용(불변식). |
| 세션 종료 시각 기록 | `InterviewSession` (전이 메서드 내부) | 서비스가 직접 `setEndedAt` 금지. |
| 평가 점수 5점 범위(1~5) | `Evaluation` 생성 시점 검증 | 범위 밖이면 생성 거부(D10, AP-6/AP-9 방지). |
| 질문 난이도 1~3 | `Question` | 생성 불변식. |
| 오프닝 질문 여부 | `Question.isOpening()` | 상태 노출이 아닌 의미 있는 질의. |
| 문답 role/followUp 일관성 | `QaLog` 생성(정적 팩터리) | 사용자 답변은 followUp 아님 등. |
| **꼬리질문 "생성" 자체** | 애플리케이션 + `LlmClient` | 도메인 밖. 도메인은 검증된 결과만 받음. |
| **LLM 응답 검증(구조/점수/필수값)** | 애플리케이션 계층 | 도메인 진입 **전** 방어(규칙 5, AP-9). |
| 질문 풀 구성/프롬프트 조립 | 애플리케이션(질문 서비스 등) | 도메인 규칙 아님. |
| 세션 설정 유효성(카테고리/개수/난이도) | `InterviewSession`(생성) 또는 `SessionSetup` VO | M2 구체화. |

**핵심 경계**: LLM은 "지능 있는 외부 협력자"다. **꼬리질문·평가 텍스트를 만드는 일은
도메인 밖**이고, 도메인은 그 결과를 **검증된 값**으로 받아 **규칙(점수·상태)** 만 집행한다.

---

## 4. 패키지 구조 (구현 정합)

> 기술 계층이 아니라 **비즈니스 개념 단위**로 나눈다. (아키텍처.md §2.5와 동일)

```
com.aiinterviewer
├── domain/                     # 규칙의 소유자 (바깥을 모른다)
│   ├── category/               # Category, CategoryPhase
│   ├── question/               # Question
│   ├── user/                   # User
│   ├── session/                # InterviewSession(애그리거트 루트), QaLog, SessionStatus, QaRole
│   └── evaluation/             # Evaluation
├── llm/                        # 외부 지능 추상화 (도메인이 의존하지 않음)
│   ├── LlmClient (interface)   # ← application이 여기에 의존 (DIP)
│   ├── dto/                    # FollowUpResult, EvaluationResult (구조화 계약)
│   └── gemini/                 # 구현 (교체 가능)
├── config/                     # SecurityConfig 등
└── common/                     # BaseTimeEntity(감사 타협 격리), 공통 유틸
```

- **Repository 인터페이스는 각 도메인 하위**에 둔다(그 개념의 영속화 계약).
- 의존성 방향: `adapter/service → domain`, `service → llm(LlmClient)`, 구현(`llm/gemini`)은
  인터페이스 뒤에 숨는다. **domain은 어떤 바깥도 import 하지 않는다**(§code-quality §1, §2 타협 제외).
- 서비스/컨트롤러/DTO는 M2에서 각 개념 하위 또는 `application/`·`adapter/`에 추가하며,
  추가 시 이 문서를 갱신한다(DoD).
