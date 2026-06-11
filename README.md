# IT News AI Briefing Agent

Spring Boot 중심 서버와 Python 크롤링 Worker가 IT 뉴스를 수집·요약하고 PostgreSQL에 저장하는 프로젝트입니다.

## Stack

- Core API: Java 8, Spring Boot 2.7.x
- Worker: Python 3.12, FastAPI, requests, BeautifulSoup4, Playwright
- Storage: PostgreSQL
- Local orchestration: Docker Compose

## Project Structure

```text
backend/    Spring Boot API, scheduler, persistence
worker/     Python crawling and AI summary worker
docs/       PRD, security policy, roadmap
```

## Local Verification

Python Worker:

```powershell
.\.venv\Scripts\python.exe -m pip install -r worker\requirements-dev.txt
.\.venv\Scripts\python.exe -m pytest worker -q
```

Spring Boot:

```powershell
mvn -q -f backend\pom.xml test
```

Docker Compose configuration:

```powershell
$env:POSTGRES_PASSWORD='local-only-password'
docker compose config
```

Local endpoints:

- Backend health: `http://localhost:13510/actuator/health`
- Latest news: `http://localhost:13510/api/news/latest`
- Paged news query: `http://localhost:13510/api/news/query`
- OpenAPI contract: `http://localhost:13510/openapi.yaml`
- Worker health: `http://localhost:13501/health`
- Worker collection contract: `POST http://localhost:13501/v1/collect`
- Worker summary contract: `POST http://localhost:13501/v1/summarize`
- PostgreSQL: `localhost:13532`

OpenClaw 연동은 [읽기 전용 API 계약](docs/OPENCLAW_API.md)을 따릅니다.

Actual crawling, database writes, and Telegram delivery remain disabled until explicitly configured and approved.

The Worker recognizes `openai`, `anthropic`, and `google-ai`, but external collection remains
blocked while `COLLECT_EXTERNAL_ENABLED=false`. AI summary and Telegram delivery are also disabled
by default.
