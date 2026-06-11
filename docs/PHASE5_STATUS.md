# Phase 5 Status

## 구현 완료

- `GET /api/news/latest`
- `GET /api/news/today`
- `GET /api/news/search?keyword=...`
- `GET /api/news/query?keyword=...&source=...&impact=...&from=...&to=...&page=...&size=...`
- 페이지형 통합 조회와 검색어·출처·중요도·UTC 기간 필터
- 페이지 크기 최대 100건 제한
- 검색어 공백 및 최대 길이 검증
- 누락 파라미터·잘못된 날짜 형식·유효성 오류의 일관된 오류 응답
- `GET /openapi.yaml` OpenAPI 3.0 명세 제공
- 읽기 전용 API와 승인 필수 실행 API 분리
- 선택적 `X-API-Key` 읽기 API 인증
- 상수 시간 API Key 비교와 키 미설정 시 fail-closed 처리
- 프로세스 단위 분당 읽기 요청 제한과 `429 RATE_LIMIT_EXCEEDED`

## 검증 완료

- 최신 뉴스 빈 결과 계약 테스트
- 잘못된 검색어 `400 Bad Request` 테스트
- 페이지형 조회 및 필터 계약 테스트
- 허용되지 않은 중요도 입력 `400 Bad Request` 테스트
- 잘못된 기간·날짜 형식·페이지 크기 `400 Bad Request` 테스트
- OpenAPI 명세 제공 테스트
- 수집 및 발송 API 기본 `403 Forbidden` 테스트
- API Key 누락 `401 Unauthorized`와 요청 제한 `429 Too Many Requests` 테스트

## 남은 완료 기준

- 다중 Backend 인스턴스 운영 시 공유 요청 제한 저장소 검토
