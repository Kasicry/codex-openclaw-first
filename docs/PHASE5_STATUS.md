# Phase 5 Status

## 구현 완료

- `GET /api/news/latest`
- `GET /api/news/today`
- `GET /api/news/search?keyword=...`
- 검색어 공백 및 최대 길이 검증
- 읽기 전용 API와 승인 필수 실행 API 분리

## 검증 완료

- 최신 뉴스 빈 결과 계약 테스트
- 잘못된 검색어 `400 Bad Request` 테스트
- 수집 및 발송 API 기본 `403 Forbidden` 테스트

## 남은 완료 기준

- 페이지네이션
- 날짜, 중요도, 출처 필터
- 일관된 오류 응답 Body
- OpenAPI 명세
- API 인증과 요청 제한
