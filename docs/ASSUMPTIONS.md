# 프로젝트 기본값과 가정

Goal 요청 수행 중 질문보다 진행을 우선하기 위해 다음 기본값을 사용한다.

| 항목 | 기본값 | 이유 |
|---|---|---|
| Java | Java 8 | 사용자 지정 기존 버전 |
| Spring Boot | 2.7.18 | Java 8을 지원하는 마지막 Spring Boot 계열 |
| Python | 프로젝트 전용 Python 3.12 가상환경 | 사용자 지정 버전이며 시스템 Python과 격리 가능 |
| Python API | FastAPI | PRD의 Spring Boot ↔ Python Worker REST 구조에 적합 |
| Java 테스트 | Maven + JUnit 5 | Spring Boot 기본 테스트 도구 |
| Python 테스트 | pytest | 간결한 fixture 기반 수집기 테스트에 적합 |
| Python 린터 | Ruff | 단일 도구로 빠른 정적 검사와 import 검사 가능 |
| 개발 테스트 DB | H2 PostgreSQL mode | Java 테스트에서 빠르고 별도 프로세스가 필요 없음 |
| 운영 저장소 | PostgreSQL | 최신 PRD의 초기 저장소 |
| 크롤링 | RSS/HTTP 우선, Playwright fallback | 실행 시간과 자원 사용을 줄임 |
| URL 중복 제거 | 추적 파라미터·fragment 제거 후 정확 일치 | 초기 단계 오탐을 줄이는 보수적 기준 |
| 외부 수집 | 기본 비활성화 | SECURITY.md의 외부 API 호출 승인 정책 준수 |
| 수집 API | 기본 `403 Forbidden` | 승인 없는 외부 실행 방지 |
| CI | GitHub Actions | 프로젝트 원격 저장소가 GitHub이며 별도 CI 요구사항 없음 |
| Docker 포트 | Backend 13510, Worker 13501, PostgreSQL 13532 | 기존 13500 포트 충돌 회피 및 프로젝트 포트 그룹 유지 |

## 미확정

- 실제 외부 수집 활성화 승인과 운영 실행 시각
- AI 요약 모델과 비용 한도
- Telegram 발송 승인 절차
- Playwright가 필요한 대상 사이트
