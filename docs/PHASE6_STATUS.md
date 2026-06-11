# Phase 6 Status

## 구현 완료

- OpenClaw가 사용할 읽기 전용 API 범위 정의
- 자연어 요청 예시와 REST API 매핑 정의
- 기간·출처 조건을 페이지형 조회 API로 변환하는 계약 정의
- 응답 필드와 오류 처리 계약 정의
- 수집·발송 쓰기 API를 OpenClaw 도구에서 제외하는 보안 경계 정의

## 검증 완료

- Backend 읽기 API 정상 응답 확인
- 잘못된 검색어의 `400 INVALID_REQUEST` 응답 확인
- OpenAPI 명세 제공과 페이지형 조회 계약 확인
- 수집 및 발송 API 기본 `403 Forbidden` 확인

## 남은 완료 기준

- 승인된 OpenClaw 환경에 읽기 전용 도구 등록
- 자연어 시나리오 3개 종단 간 검증
- OpenClaw 호출 계정 인증과 요청 제한 적용
- 응답에 요약·중요도 필드 추가 후 브리핑 표현 검증

연동 규격은 [OPENCLAW_API.md](./OPENCLAW_API.md)를 따른다. 실제 OpenClaw 환경
변경과 외부 호출은 [SECURITY.md](./SECURITY.md)에 따른 승인 전까지 수행하지 않는다.
