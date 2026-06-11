# Phase 4 Status

## 구현 완료

- Telegram Bot API Client
- 4,000자 이하 브리핑 발송 요청 검증
- `POST /api/briefing/send`
- 발송 기본 비활성화 및 `403 Forbidden`
- Token과 Chat ID 환경 변수 주입

## 검증 완료

- 기본 비활성화 API 테스트
- 비밀정보가 저장소 설정에 포함되지 않음

## 남은 완료 기준

- 승인된 테스트 채널 실제 발송
- 긴 브리핑 분할 발송
- 중복 발송 방지
- 실패 재시도와 Spring Scheduler 연동

Telegram 발송은 [SECURITY.md](./SECURITY.md)에 따라 명시적 승인 전까지
`TELEGRAM_SEND_ENABLED=false`로 유지한다.
