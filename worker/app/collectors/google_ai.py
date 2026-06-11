import requests

from app.collectors.rss import parse_rss_articles
from app.models import Article
from app.services.http import create_retry_session

GOOGLE_AI_RSS_URL = "https://blog.google/technology/ai/rss/"


class GoogleAINewsCollector:
    def __init__(
        self,
        enabled: bool = False,
        session: requests.Session | None = None,
        timeout_seconds: int = 30,
    ) -> None:
        self._enabled = enabled
        self._session = session or create_retry_session()
        self._timeout_seconds = timeout_seconds

    def __call__(self, keywords: list[str]) -> list[Article]:
        if not self._enabled:
            raise RuntimeError("external collection is disabled")

        response = self._session.get(
            GOOGLE_AI_RSS_URL,
            headers={"User-Agent": "openclaw-news-briefing/0.1"},
            timeout=self._timeout_seconds,
        )
        response.raise_for_status()
        return parse_rss_articles(response.text, "google-ai", keywords)
