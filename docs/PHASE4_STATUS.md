# Phase 4 Status

## 구현 완료

- Telegram Bot API Client
- 긴 브리핑의 4,000자 단위 분할 발송
- Unicode surrogate pair를 보존하는 메시지 분할
- `POST /api/briefing/send`
- `GET /api/briefing/preview/today` 발송 전 미리보기
- `POST /api/briefing/send/today` 생성된 일일 브리핑 수동 실행
- 발송 기본 비활성화 및 `403 Forbidden`
- Token과 Chat ID 환경 변수 주입
- Telegram 발송 실패 최대 3회 재시도
- 해당 날짜의 미발송 기사만 조회하는 일일 브리핑 생성
- 전체 청크 발송 성공 후 `notification_sent=true` 저장
- Spring Scheduler 일일 실행 경로
- Scheduler와 Telegram 발송의 독립적인 기본 비활성화

## 검증 완료

- 기본 비활성화 API 테스트
- 비밀정보가 저장소 설정에 포함되지 않음
- 긴 메시지 분할 및 Unicode 경계 테스트
- Telegram 일시 실패 재시도 테스트
- 발송 성공·실패에 따른 알림 상태 저장 테스트
- Scheduler 활성화 조건 테스트
- 미리보기 무발송·무상태변경 및 읽기 API 보호 테스트
- 생성된 일일 브리핑 수동 실행 승인 게이트 테스트

## 남은 완료 기준

- 승인된 테스트 채널 실제 발송
- 실제 Telegram 응답과 운영 재시도 간격 검증
- 발송 성공 직후 DB 저장 실패 시 발생할 수 있는 중복 발송 보완

Telegram 발송은 [SECURITY.md](./SECURITY.md)에 따라 명시적 승인 전까지
`TELEGRAM_SEND_ENABLED=false`, `SCHEDULE_ENABLED=false`로 유지한다.
