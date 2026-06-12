# 운영 지표와 장애 대응

이 문서는 Phase 7 운영 기본선이다. 실제 외부 호출, 메시지 발송, DB 변경, 서비스
재시작과 복구 실행은 [SECURITY.md](./SECURITY.md)에 따라 사전 승인을 받아야 한다.

## 관측 지점

- Backend 상태: `GET /actuator/health`
- Backend 지표 목록: `GET /actuator/metrics`
- 개별 지표: `GET /actuator/metrics/{metric.name}`
- Worker 상태: `GET /health`

Actuator는 내부망 또는 관리용 Reverse Proxy 뒤에서만 노출한다. 지표 태그에는 기사
제목, URL, 오류 원문, 토큰을 넣지 않는다. 수집기 `source` 태그는 `openai`,
`anthropic`, `google-ai`, `other`로 제한한다.

## 애플리케이션 지표

| 지표 | 태그 | 의미 |
|---|---|---|
| `openclaw.collection.runs` | `result=success|partial|failure` | 수집 실행 결과 |
| `openclaw.collection.duration` | 없음 | Backend 수집 처리 시간 |
| `openclaw.collection.sources` | `source`, `result` | 사이트별 수집 결과 |
| `openclaw.collection.articles` | `result=received|saved|existing|duplicate` | 수집 기사 처리 건수. `existing`은 DB URL 중복, `duplicate`는 Worker 통합 건수 |
| `openclaw.summary.runs` | `result=success|failure` | 요약 처리 결과 |
| `openclaw.summary.duration` | 없음 | 요약 처리 시간 |
| `openclaw.briefing.runs` | `result=success|skipped|failure` | 일일 브리핑 결과 |
| `openclaw.briefing.duration` | 없음 | 일일 브리핑 처리 시간 |
| `openclaw.briefing.articles` | `result=sent` | 발송 완료 기사 건수 |
| `openclaw.briefing.chunks` | `result=sent` | 발송 완료 Telegram 청크 수 |

프로세스 재시작 시 현재 Micrometer 메모리 지표는 초기화된다. 장기 추세가 필요해지는
시점에는 Prometheus 같은 외부 수집기를 연결한다.

## 초기 경보 기준

실데이터 기준선이 쌓이기 전까지 다음 보수적 기준을 사용한다.

| 심각도 | 조건 | 초기 대응 |
|---|---|---|
| Critical | Backend 또는 Worker health가 2분 이상 비정상 | 쓰기·외부 작업을 비활성화하고 로그와 의존 서비스 상태 확인 |
| Critical | 예약 시각 이후 브리핑 `failure` 1회 이상 | 재발송 전 DB의 `notification_sent` 상태와 Telegram 발송 이력 확인 |
| Warning | 수집 `failure` 1회 또는 `partial` 3회 연속 | 실패 사이트만 격리하여 응답 구조·접근 정책 확인 |
| Warning | 수집 처리 시간 p95가 5분 초과 | 사이트별 처리 시간과 재시도 횟수 확인 |
| Warning | 요약 실패율이 15분 동안 10% 초과, 최소 5건 | Provider 응답·속도 제한·출력 스키마 확인 |
| Warning | PostgreSQL 디스크 사용량 70% 초과 | 증가율과 보존 정책 점검, 85% 전에 용량 확장 |

## 장애 대응 절차

1. `health`, 지표, 애플리케이션 로그를 조회하여 영향 범위를 확인한다.
2. 외부 수집·요약·발송 플래그가 기본 비활성화인지 확인한다.
3. 실패가 단일 사이트인지, Worker 전체인지, DB인지 분류한다.
4. 재처리 전에 중복 저장·중복 발송 가능성과 현재 상태를 조회한다.
5. 사용자 승인 후 필요한 작업만 일시 활성화해 재처리한다.
6. 성공 여부를 지표와 저장 상태로 검증하고 작업 플래그를 다시 비활성화한다.
7. 원인, 영향, 재처리 범위, 검증 결과를 `Logs/YYYY-MM-DD.md`에 기록한다.

### 재처리 기준

- 수집 실패: 성공한 사이트 결과는 보존하고 실패 사이트 접근 문제를 해결한 후 전체
  수집을 재실행한다. URL unique 제약과 애플리케이션 중복 검사가 재저장을 방지한다.
- 요약 실패: `collection_status=SUMMARY_FAILED`인 기사 ID를 조회한 뒤 승인된 기사만
  `POST /api/news/{articleId}/summarize`로 재처리한다.
- 브리핑 실패: `notification_sent=false`인 기사와 Telegram 발송 이력을 먼저
  대조한다. 발송 성공 후 DB 저장이 실패한 경우 중복 발송 위험이 있으므로 자동
  재발송하지 않는다.

## 백업·복구와 보존

- PostgreSQL을 원본 저장소로 사용하며 일별 JSON 내보내기는 현재 사용하지 않는다.
- 운영 전환 시 매일 논리 백업을 생성하고 30일 보존한다.
- 월 1회 복구 연습을 별도 DB에서 수행하고 기사 수, 최신 발행일, Flyway 버전을
  원본과 비교한다.
- 기사와 요약은 365일 보존을 초기값으로 사용한다. 원문 장기 보존 필요성과 라이선스
  검토 후 기간을 조정한다.
- 애플리케이션 로그는 30일, 지표 추세는 90일 보존을 초기값으로 사용한다.
- 삭제, 복구, 보존 기간 변경은 DB 수정 또는 운영 작업이므로 사전 승인이 필요하다.

### 별도 DB 복구 연습

`scripts/postgres-restore-drill.ps1`은 Docker Compose PostgreSQL의 논리 백업을 별도
`*_restore_drill` DB에 복구하고 다음 값을 원본과 비교한 뒤 연습 DB와 임시 dump를
정리한다.

- 기사 수
- 기사 키워드 수
- 관련 출처 수
- 최신 발행일
- 최근 성공 Flyway 버전

스크립트는 기존 복구 연습 DB가 있으면 중단하고 원본과 같은 DB 이름을 허용하지
않는다. 별도 DB 생성·복구·삭제와 임시 dump 삭제가 포함되므로 실행 전 승인이
필요하다.

```powershell
$env:POSTGRES_PASSWORD='local-only-password'
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\postgres-restore-drill.ps1
```

성공 출력의 `fingerprint`에는 위 다섯 값만 포함되며 기사 본문, URL, 비밀정보는
포함하지 않는다. 실패 시 원본 DB는 수정하지 않고 생성한 연습 DB와 임시 dump만
정리한다.

## 확장 판단 기준

- Backend 인스턴스가 2개 이상이면 현재 프로세스 단위 요청 제한을 Redis 기반 공유
  제한으로 교체한다.
- 일별 기사 수가 10,000건 또는 전체 기사 수가 100만 건에 근접하면 실제 쿼리
  `EXPLAIN ANALYZE` 결과로 인덱스를 재검토한다.
- p95 조회 시간이 500ms를 넘거나 DB CPU가 지속적으로 70%를 넘으면 캐시, 읽기
  복제본, 비동기 처리를 순서대로 검토한다.
- 수집·요약 작업이 5분 목표를 반복적으로 넘을 때만 Queue 또는 Quartz 도입을
  검토한다.

## 조회 성능 점검

`scripts/explain-news-queries.sql`은 최신, 오늘, 출처, 중요도, 미발송 브리핑 조회의
읽기 전용 실행 계획 기준선이다. 운영 DB에서는 조회 전용 계정으로만 실행하고 결과의
기사 본문이나 URL을 로그에 남기지 않는다.

```powershell
psql -X -v ON_ERROR_STOP=1 -f scripts/explain-news-queries.sql
```

- 전체 기사 수가 적을 때 PostgreSQL이 순차 스캔을 선택하는 것은 정상이다.
- 데이터가 증가한 뒤에도 필터 조회가 순차 스캔이면 실제 조건의 선택도와 통계를
  확인하고 `ANALYZE` 또는 인덱스 조정을 승인받아 수행한다.
- `title`의 `%keyword%` 검색은 일반 B-tree 인덱스를 사용할 수 없다. p95가 500ms를
  넘을 때만 승인 후 `pg_trgm` 또는 별도 검색 저장소를 검토한다.
- V3 migration은 출처·중요도·미발송 여부와 최신 발행일 정렬을 결합한 복합
  인덱스를 추가한다. 기존 단일 인덱스 제거는 실데이터 실행 계획 확인 전 보류한다.
