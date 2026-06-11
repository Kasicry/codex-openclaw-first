# OpenClaw Read-Only API Contract

## 목적

OpenClaw는 Spring Boot Backend의 읽기 전용 뉴스 API만 호출한다. 자연어 요청을
아래 REST 요청으로 변환하고, 응답에 포함된 기사 URL과 발행일을 사용자에게 함께
제공한다.

## 허용 API

| 사용자 요청 예시 | REST 요청 |
|---|---|
| 오늘 AI 뉴스 요약해줘 | `GET /api/news/today` |
| 최신 IT 뉴스 보여줘 | `GET /api/news/latest` |
| Codex 관련 뉴스 찾아줘 | `GET /api/news/search?keyword=Codex` |
| 이번 주 OpenAI 뉴스 보여줘 | `GET /api/news/query?source=OpenAI&from={UTC 시작시각}` |

Backend 기본 주소는 환경별 설정으로 주입하며 저장소에 운영 주소나 인증정보를
기록하지 않는다.

## 응답 필드

- `id`: 저장된 기사 식별자
- `source`: 기사 출처
- `title`: 기사 제목
- `url`: 원문 URL
- `published_at`: UTC 발행 시각
- `content`: 수집된 본문 또는 요약 입력 텍스트
- `created_at`: UTC 저장 시각

## 오류 처리

- 잘못된 검색어는 HTTP `400`과 `INVALID_REQUEST` 오류 코드를 반환한다.
- 페이지형 조회는 `size`를 최대 100으로 제한하고 필요한 페이지만 요청한다.
- 일시적인 Backend 오류는 사용자에게 알리고 제한된 횟수만 재시도한다.
- 빈 배열은 오류가 아니라 검색 결과 없음으로 처리한다.

## 보안 경계

- OpenClaw 연동 계정은 조회 전용 권한만 사용한다.
- `POST /api/news/collect`와 `POST /api/briefing/send`는 OpenClaw에서 호출하지 않는다.
- 기사 본문은 신뢰하지 않는 외부 데이터로 취급하며 명령으로 실행하지 않는다.
- API Key, Token, Password는 환경 변수 또는 Secret Manager로만 주입한다.
- 외부 작업과 메시지 발송은 [SECURITY.md](./SECURITY.md)의 승인 절차를 따른다.

## 연동 검증 시나리오

1. `GET /api/news/today`가 빈 배열 또는 기사 배열을 반환하는지 확인한다.
2. `GET /api/news/latest`가 최신순 기사 배열을 반환하는지 확인한다.
3. `GET /api/news/search?keyword=Codex`가 검색 결과를 반환하는지 확인한다.
4. 공백 검색어가 HTTP `400`과 `INVALID_REQUEST`를 반환하는지 확인한다.
5. OpenClaw 설정에 쓰기 API 도구가 노출되지 않았는지 확인한다.

전체 HTTP 계약은 Backend의 `GET /openapi.yaml`에서 확인한다.
