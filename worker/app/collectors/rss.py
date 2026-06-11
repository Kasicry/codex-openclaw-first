from collections.abc import Iterable
from datetime import timezone
from email.utils import parsedate_to_datetime

from bs4 import BeautifulSoup
from defusedxml import ElementTree

from app.models import Article
from app.services.extractor import compact_text


def parse_rss_articles(xml_text: str, source: str, keywords: Iterable[str]) -> list[Article]:
    root = ElementTree.fromstring(xml_text)
    normalized_keywords = [keyword.casefold() for keyword in keywords if keyword.strip()]
    articles: list[Article] = []

    for item in root.findall(".//item"):
        title = compact_text(item.findtext("title") or "")
        url = compact_text(item.findtext("link") or "")
        description_html = item.findtext("description") or ""
        description = compact_text(BeautifulSoup(description_html, "html.parser").get_text(" "))

        if not title or not url:
            continue

        searchable = f"{title} {description}".casefold()
        matches_keyword = any(keyword in searchable for keyword in normalized_keywords)
        if normalized_keywords and not matches_keyword:
            continue

        published_at = None
        published_text = item.findtext("pubDate")
        if published_text:
            published_at = parsedate_to_datetime(published_text)
            if published_at.tzinfo is None:
                published_at = published_at.replace(tzinfo=timezone.utc)

        articles.append(
            Article(
                source=source,
                title=title,
                url=url,
                published_at=published_at,
                content=description,
            )
        )

    return articles
