# Phase 1 Status

## 구현 완료

- OpenAI News RSS 수집기
- Google AI Blog RSS 수집기
- Anthropic Newsroom HTML 수집기
- 키워드 필터링
- 안전한 XML 파싱
- HTTP GET 재시도 및 backoff
- 사이트별 실패 격리
- URL 정규화 및 정확 URL 중복 제거
- 사이트별·전체 소요시간과 실패 결과 기록
- Spring Boot 수집 결과 저장과 재수집 URL 중복 방지
- 기사별 매칭 키워드 기록
- 중복 통합 후 관련 출처와 매칭 키워드 PostgreSQL 저장

## 검증 완료

- 3개 사이트 parser fixture 테스트
- 한 사이트 실패 시 다른 사이트 수집 지속 테스트
- URL 정규화·중복 제거 테스트
- Python Worker ↔ Spring Boot 응답 계약 테스트
- 동일 URL 재수집 시 DB 중복 저장 방지 테스트
- 매칭 키워드·관련 출처 Worker 계약 및 저장 테스트

## 남은 완료 기준

- 승인된 실제 외부 수집으로 3개 사이트 접근 가능 여부 확인
- 실제 수집 결과 PostgreSQL 저장 확인
- 정상 네트워크에서 전체 작업 5분 이내 완료 확인

외부 수집은 [SECURITY.md](./SECURITY.md)에 따라 명시적 승인 전까지
`COLLECT_EXTERNAL_ENABLED=false`로 유지한다.
