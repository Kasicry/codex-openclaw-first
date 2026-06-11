from datetime import datetime
from urllib.parse import urljoin

import requests
from bs4 import BeautifulSoup

from app.models import Article
from app.services.extractor import compact_text
from app.services.http import create_retry_session

ANTHROPIC_NEWS_URL = "https://www.anthropic.com/news"


class AnthropicNewsCollector:
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
            ANTHROPIC_NEWS_URL,
            headers={"User-Agent": "openclaw-news-briefing/0.1"},
            timeout=self._timeout_seconds,
        )
        response.raise_for_status()
        return self.parse(response.text, keywords)

    @staticmethod
    def parse(html: str, keywords: list[str]) -> list[Article]:
        soup = BeautifulSoup(html, "html.parser")
        normalized_keywords = [keyword.casefold() for keyword in keywords if keyword.strip()]
        articles: list[Article] = []
        seen_urls: set[str] = set()

        for link in soup.select('a[href^="/news/"]'):
            url = urljoin(ANTHROPIC_NEWS_URL, link.get("href", ""))
            if url in seen_urls:
                continue

            heading = link.find(["h2", "h3", "h4"])
            title = compact_text((heading or link).get_text(" "))
            content = compact_text(link.get_text(" "))
            if not title:
                continue

            searchable = f"{title} {content}".casefold()
            if normalized_keywords and not any(
                keyword in searchable for keyword in normalized_keywords
            ):
                continue

            published_at = None
            time_tag = link.find("time")
            if time_tag and time_tag.get("datetime"):
                published_at = datetime.fromisoformat(time_tag["datetime"].replace("Z", "+00:00"))

            seen_urls.add(url)
            articles.append(
                Article(
                    source="anthropic",
                    title=title,
                    url=url,
                    published_at=published_at,
                    content=content,
                )
            )

        return articles
