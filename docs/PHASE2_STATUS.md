# Phase 2 Status

## 구현 완료

- 기사 제목·본문 입력 계약
- 제목, 핵심 내용, 영향도, 개발자 관점의 구조화 출력 계약
- OpenAI Chat Completions 직접 호출 Provider
- 기사 본문을 신뢰하지 않는 데이터로 취급하는 시스템 프롬프트
- JSON 구조 검증
- 저장된 기사를 Worker 요약 API에 전달하는 Backend 경로
- 요약, 중요도, 개발자 관점, 처리 상태 PostgreSQL 저장 모델
- 요약 성공 시 `SUMMARIZED`, 실패 시 `SUMMARY_FAILED` 상태 기록
- `POST /api/news/{articleId}/summarize` 기본 비활성화 및 `403 Forbidden`
- AI 요약 기본 비활성화 및 `403 Forbidden`

## 검증 완료

- 외부 API를 호출하지 않는 fake session 계약 테스트
- 구조화 응답 파싱 테스트
- Worker 요약 응답 매핑과 Backend 저장 테스트
- Backend 요약 API 기본 비활성화 테스트
- 기본 비활성화 API 테스트

## 남은 완료 기준

- 승인된 API Key와 모델로 실제 기사 요약 생성 확인
- 샘플 기사 세트 품질 검토
- 비용과 처리 시간 측정

AI 요약은 [SECURITY.md](./SECURITY.md)에 따라 명시적 승인 전까지
`AI_SUMMARY_ENABLED=false`로 유지한다.
