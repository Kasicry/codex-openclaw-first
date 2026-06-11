# Phase 2 Status

## 구현 완료

- 기사 제목·본문 입력 계약
- 제목, 핵심 내용, 영향도, 개발자 관점의 구조화 출력 계약
- OpenAI Chat Completions 직접 호출 Provider
- 기사 본문을 신뢰하지 않는 데이터로 취급하는 시스템 프롬프트
- JSON 구조 검증
- AI 요약 기본 비활성화 및 `403 Forbidden`

## 검증 완료

- 외부 API를 호출하지 않는 fake session 계약 테스트
- 구조화 응답 파싱 테스트
- 기본 비활성화 API 테스트

## 남은 완료 기준

- 승인된 API Key와 모델로 실제 기사 요약 생성 확인
- 샘플 기사 세트 품질 검토
- 비용과 처리 시간 측정

AI 요약은 [SECURITY.md](./SECURITY.md)에 따라 명시적 승인 전까지
`AI_SUMMARY_ENABLED=false`로 유지한다.
