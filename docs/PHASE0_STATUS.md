# Phase 0 Status

## 완료

- Java 8 대상 Spring Boot 2.7.x 중심 서버 골격
- 프로젝트 전용 Python 3.12.10 가상환경
- FastAPI 기반 Python Worker 골격
- Spring Boot ↔ Python Worker REST 수집 계약
- PostgreSQL 뉴스 기사 스키마 및 Flyway migration
- 읽기 전용 뉴스 API 골격
- 명시적 활성화 전 수집 `POST` 차단
- 사이트별 실패 격리 coordinator와 테스트
- BeautifulSoup 본문 fallback 추출과 테스트
- Docker Compose 구성 및 healthcheck
- starterkit 재사용 범위 검토
- Docker Compose 서비스 실제 기동
- PostgreSQL 대상 migration 및 저장 흐름 검증

## 검증 완료

- Python Worker 단위·API 테스트
- Spring Boot context, 보안 기본값, Worker 계약 테스트
- Docker Compose 구성 문법
- Java 8 대상 바이트코드

## 남은 작업

- 없음. 이후 기능 작업은 Phase 1 이상 상태 문서에서 관리한다.

## 환경 특이사항

- 로컬 Java 8은 JRE만 존재하며 JDK 8 컴파일러는 없음
- Maven은 JDK 18에서 실행하되 Java 8 대상 바이트코드를 생성하도록 구성
- Docker Backend는 실제 Java 8 런타임으로 기동됨
- PostgreSQL 16은 Flyway 8.5 공식 검증 범위 밖이라는 경고가 있으나 migration은 성공함
