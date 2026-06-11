IT News AI Briefing Agent
프로젝트 개요
IT 및 AI 관련 뉴스를 수집하고 분석하여 사용자에게 요약 제공하는 AI 뉴스 브리핑 시스템을 구축한다.
초기 단계에서는 codex-crawling-starterkit을 기반으로 구현한다.
향후 API-LISTNER-STARTERKIT 및 OpenClaw와 연동하여 대화형 AI 에이전트로 확장한다.
________________________________________
목표
사용자가 여러 IT 뉴스 사이트를 직접 방문하지 않아도
하루 또는 주간 기준으로 중요한 IT 뉴스를 요약하여 확인할 수 있도록 한다.
________________________________________
문제 정의
현재
•	OpenAI
•	Claude
•	Gemini
•	Codex
•	Oracle
•	PostgreSQL
•	MCP
•	AI Agent
관련 뉴스가 여러 사이트에 분산되어 있다.
사용자는 직접 방문하여 확인해야 한다.
뉴스가 많아 중요한 내용을 놓칠 수 있다.
________________________________________
핵심 기능
뉴스 수집
대상 사이트
해외
•	OpenAI Blog
•	Anthropic News
•	Google AI Blog
•	Microsoft AI Blog
•	GitHub Blog
•	Hugging Face Blog
국내
•	AI타임스
•	전자신문 AI
•	ZDNet Korea
•	ITWorld Korea
________________________________________
뉴스 필터링
다음 키워드 포함 기사 우선 수집
•	AI
•	LLM
•	Agent
•	MCP
•	OpenAI
•	Codex
•	Claude
•	Gemini
•	DeepSeek
•	Oracle
•	PostgreSQL
•	Docker
•	Kubernetes
________________________________________
AI 요약
기사당
•	제목
•	핵심 내용
•	영향도
•	개발자 관점 요약
생성
예시
제목: GPT-6 Preview Released
핵심 내용: OpenAI가 GPT-6 Preview 공개
영향도: 높음
개발자 관점: Codex 성능 향상 가능성 존재
________________________________________
중복 제거
동일 뉴스가 여러 사이트에 게시된 경우
대표 기사만 유지
________________________________________
중요도 분류
등급
•	높음
•	보통
•	낮음
________________________________________
비기능 요구사항
성능
뉴스 수집
5분 이내 완료
________________________________________
안정성
특정 사이트 수집 실패 시
다른 사이트 수집 계속 진행
________________________________________
보안
[SECURITY.md](./SECURITY.md) 준수
•	비밀키 저장 금지
•	승인 없는 외부 실행 금지
________________________________________
데이터 저장
초기
JSON 파일
예시
/news
2026-06-11.json
향후
SQLite
PostgreSQL
확장 가능
________________________________________
개발 단계
Phase 1
뉴스 수집
완료 기준
•	3개 사이트 이상 수집 가능
________________________________________
Phase 2
AI 요약
완료 기준
•	기사 요약 생성
________________________________________
Phase 3
중복 제거
완료 기준
•	유사 기사 통합
________________________________________
Phase 4
Telegram 알림
완료 기준
•	요약 자동 발송
________________________________________
Phase 5
API-LISTNER 연동
완료 기준
•	REST API 제공
예시
GET /news/latest
GET /news/today
GET /news/search
________________________________________
Phase 6
OpenClaw 연동
완료 기준
사용자 질의 가능
예시
“오늘 AI 뉴스 요약”
“이번주 OpenAI 뉴스”
“Codex 관련 뉴스”
________________________________________
성공 기준
사용자가 하루 1회 실행하여
5분 이내에
중요 IT 뉴스를 확인할 수 있다.
________________________________________
제외 범위
초기 버전에서는
•	댓글 분석
•	SNS 분석
•	유료 뉴스 크롤링
•	실시간 스트리밍 수집
은 구현하지 않는다.
________________________________________

기술 스택
전체 구조
본 프로젝트는 Spring Boot를 중심 서버로 사용하고, Python은 크롤링 및 AI 요약 전용 워커로 사용한다.
Spring Boot는 API, 인증, 데이터 저장, OpenClaw 연동, 알림 발송을 담당한다.
Python Worker는 IT 뉴스 수집, 본문 추출, AI 요약, 키워드 분석을 담당한다.
________________________________________
Backend Core
•	Java 8
•	Spring Boot 2.7.x
•	Spring Web
•	Spring Validation
•	Spring Scheduler
•	Spring Data JPA 또는 MyBatis
역할:
•	REST API 제공
•	뉴스 데이터 저장/조회
•	작업 실행 요청 관리
•	OpenClaw 연동 API 제공
•	Telegram 알림 발송
•	AD, EAI, Guacamole, DB 모니터링 확장 시 중심 서버 역할
________________________________________
AI / Crawling Worker
•	Python 3.12
•	requests
•	BeautifulSoup4
•	Playwright
•	OpenAI API 또는 호환 LLM API
역할:
•	IT 뉴스 사이트 크롤링
•	기사 본문 추출
•	중복 후보 탐지
•	AI 요약 생성
•	중요도 분류
•	개발자 관점 코멘트 생성
________________________________________
Communication
Spring Boot와 Python Worker는 REST API 방식으로 연동한다.
초기 방식:
•	Spring Boot → Python Worker 호출
•	Python Worker → 수집/요약 결과 반환
•	Spring Boot → 결과 저장
향후 확장:
•	메시지 큐 기반 비동기 처리 검토
•	Redis Queue 또는 RabbitMQ 검토
________________________________________
Storage
초기:
•	PostgreSQL
주요 저장 데이터:
•	뉴스 원문 메타데이터
•	기사 URL
•	제목
•	발행일
•	출처
•	요약
•	중요도
•	키워드
•	수집 상태
•	알림 발송 여부
개발 테스트용:
•	H2 또는 SQLite 선택 가능
________________________________________
Scheduler
초기:
•	Spring Scheduler
역할:
•	매일 정해진 시간 뉴스 수집 요청
•	실패 작업 재시도
•	알림 발송 트리거
향후:
•	Quartz Scheduler 검토
________________________________________
Notification
•	Telegram Bot API
역할:
•	일일 IT 뉴스 브리핑 발송
•	중요도 높은 뉴스 즉시 알림
•	OpenClaw 연동 전 임시 인터페이스 역할
________________________________________
OpenClaw Integration
OpenClaw는 Spring Boot API를 호출하는 방식으로 연동한다.
예시 API:
•	GET /api/news/today
•	GET /api/news/latest
•	GET /api/news/search?keyword=codex
•	POST /api/news/collect
•	POST /api/briefing/send
OpenClaw 예시 명령:
•	오늘 AI 뉴스 요약해줘
•	이번 주 OpenAI 뉴스 보여줘
•	Codex 관련 뉴스만 찾아줘
•	중요도 높은 뉴스만 브리핑해줘
________________________________________
Future Monitoring Integration
향후 Spring Boot 중심 서버에 다음 모듈을 추가한다.
AD Monitoring
•	LDAP 조회
•	인증서 만료일 확인
•	계정 잠금 상태 확인
EAI Monitoring
•	API Listener 이벤트 수신
•	실패 로그 수집
•	재처리 대상 조회
Guacamole Monitoring
•	접속 이력 조회
•	현재 접속자 조회
•	비정상 접속 패턴 탐지
DB Monitoring
•	PostgreSQL 세션 조회
•	Oracle 세션 조회
•	Lock 확인
•	장시간 실행 쿼리 조회
________________________________________
Security
•	[SECURITY.md](./SECURITY.md) 정책 준수
•	기본 계정은 조회 전용 권한 사용
•	DB 변경 작업은 사용자 승인 후 수행
•	운영 서버 명령 실행은 사용자 승인 후 수행
•	API Key, Token, Password는 환경변수 또는 Secret Manager로 관리
•	로그에는 비밀정보와 개인정보를 마스킹한다
________________________________________
Deployment
초기:
•	Docker Compose
구성:
•	Spring Boot App
•	Python Worker
•	PostgreSQL
향후:
•	운영 환경 분리
•	내부망 배포
•	Reverse Proxy 적용
•	OpenClaw 연동 컨테이너 추가
