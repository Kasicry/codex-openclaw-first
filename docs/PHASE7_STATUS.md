# Phase 7 Status

## 구현 완료

- 사이트별 성공·실패·처리 시간 구조화 로그
- 수집 결과의 중복 수와 전체 처리 시간 기록
- 기사 처리 상태와 알림 발송 여부 저장 모델
- Flyway 기반 PostgreSQL schema version 관리
- GitHub Actions 기반 Java·Python·Compose 검증
- 외부 수집·AI 요약·Telegram 발송 기본 비활성화
- Telegram Scheduler 기본 비활성화 및 발송 상태 기반 중복 방지
- 프로세스 단위 읽기 API 요청 제한
- 수집·사이트별 결과·요약·브리핑 Micrometer 지표
- `/actuator/metrics` 내부 운영 조회 경로
- 초기 경보 기준과 장애 분류·승인 기반 재처리 절차
- PostgreSQL 백업·복구 검증 및 데이터·로그·지표 보존 정책
- 다중 인스턴스 요청 제한과 저장소 확장 판단 기준
- 일별 JSON 내보내기 보류 결정
- 주요 읽기 쿼리용 복합 인덱스와 읽기 전용 실행 계획 점검 스크립트
- 출처 저장·필터 값 정규화와 조회 성능 점검 기준

## 검증 완료

- H2에서 Flyway V1·V2·V3 migration과 JPA schema validation 통과
- PostgreSQL에서 승인된 Flyway V2 migration과 schema v2 확인
- PostgreSQL에서 승인된 Flyway V3 migration과 신규 복합 인덱스 3개 확인
- 읽기 전용 `EXPLAIN ANALYZE` 기준선 측정 및 출처·중요도 복합 인덱스 사용 확인
- Worker 실패 격리와 구조화 결과 테스트
- 저장소 비밀정보 패턴 검사
- 지표 결과·처리 건수·저카디널리티 source 태그 단위 테스트

## 남은 완료 기준

- 실데이터 축적 후 조회 p95와 실행 계획 추세 측정
- 실제 데이터 기준 품질·성능 추세 측정
- 외부 모니터링 시스템과 실제 실패 알림 연동
- 별도 DB에서 승인된 백업 복구 연습
