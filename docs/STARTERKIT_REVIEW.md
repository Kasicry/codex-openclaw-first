# codex-crawling-starterkit 검토

## 기준 저장소

- 저장소: `https://github.com/Kasicry/codex-crawling-starterkit.git`
- 검토 브랜치: `main`
- 검토 커밋: `77823716167226748dcd75786afcf94b3c8ef5d7`

## 재사용할 패턴

- `BeautifulSoup4` 기반 본문 fallback 추출
- 동적 페이지는 Playwright, 정적 페이지는 HTTP 요청을 사용하는 단계적 fallback
- 사이트 또는 작업 단위 실패 격리
- 작업 상태, 오류 메시지, 시작·종료 시각 보존
- PostgreSQL URL 및 상태 인덱스
- Docker Compose healthcheck

## 현재 프로젝트에 직접 재사용하지 않는 구성

- Next.js Web/API
- BullMQ 및 Redis Worker
- Prisma 데이터 모델
- Node.js에서 Python 프로세스를 직접 실행하는 구조

현재 PRD는 Spring Boot가 중심 서버이고 Python Worker와 REST API로 통신하도록 정의하므로 위 구성은 패턴만 참고한다.

## 후속 검토

- 대상 뉴스 사이트별 robots.txt, 이용약관, 요청 제한
- Playwright 브라우저 이미지 크기와 실행 시간
- 메시지 큐 도입 시 BullMQ 대신 Redis Queue 또는 RabbitMQ 선택
