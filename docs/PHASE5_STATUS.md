# Phase 5 Status

## 구현 완료

- `GET /api/news/latest`
- `GET /api/news/today`
- `GET /api/news/search?keyword=...`
- `GET /api/news/query?keyword=...&source=...&from=...&to=...&page=...&size=...`
- 페이지형 통합 조회와 검색어·출처·UTC 기간 필터
- 페이지 크기 최대 100건 제한
- 검색어 공백 및 최대 길이 검증
- 누락 파라미터·잘못된 날짜 형식·유효성 오류의 일관된 오류 응답
- `GET /openapi.yaml` OpenAPI 3.0 명세 제공
- 읽기 전용 API와 승인 필수 실행 API 분리

## 검증 완료

- 최신 뉴스 빈 결과 계약 테스트
- 잘못된 검색어 `400 Bad Request` 테스트
- 페이지형 조회 및 필터 계약 테스트
- 잘못된 기간·날짜 형식·페이지 크기 `400 Bad Request` 테스트
- OpenAPI 명세 제공 테스트
- 수집 및 발송 API 기본 `403 Forbidden` 테스트

## 남은 완료 기준

- 중요도 필터
- API 인증과 요청 제한
