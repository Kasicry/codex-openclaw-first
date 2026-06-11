import json
import logging
from collections.abc import Callable
from time import perf_counter

from app.models import Article, CollectResponse, SourceResult
from app.services.deduplicator import deduplicate_articles
from app.services.url import normalize_url

Collector = Callable[[list[str]], list[Article]]
logger = logging.getLogger("uvicorn.error")


class CollectionCoordinator:
    def __init__(self, collectors: dict[str, Collector] | None = None) -> None:
        self._collectors = collectors or {}

    def collect(self, sources: list[str], keywords: list[str]) -> CollectResponse:
        started_at = perf_counter()
        collected_articles: list[Article] = []
        source_results: list[SourceResult] = []

        for source in sources:
            source_started_at = perf_counter()
            collector = self._collectors.get(source)
            if collector is None:
                source_results.append(
                    SourceResult(
                        source=source,
                        status="failed",
                        duration_ms=_elapsed_ms(source_started_at),
                        error="collector is not configured",
                    )
                )
                continue

            try:
                collected = collector(keywords)
                for article in collected:
                    normalized_url = normalize_url(str(article.url))
                    collected_articles.append(article.model_copy(update={"url": normalized_url}))
                source_results.append(
                    SourceResult(
                        source=source,
                        status="completed",
                        article_count=len(collected),
                        duration_ms=_elapsed_ms(source_started_at),
                    )
                )
            except Exception as exc:
                source_results.append(
                    SourceResult(
                        source=source,
                        status="failed",
                        duration_ms=_elapsed_ms(source_started_at),
                        error=str(exc),
                    )
                )

        articles, duplicate_count = deduplicate_articles(collected_articles)
        response = CollectResponse(
            articles=articles,
            sources=source_results,
            duplicate_count=duplicate_count,
            duration_ms=_elapsed_ms(started_at),
        )
        logger.info(
            json.dumps(
                {
                    "event": "collection_completed",
                    "article_count": len(articles),
                    "duplicate_count": duplicate_count,
                    "duration_ms": response.duration_ms,
                    "sources": [result.model_dump() for result in source_results],
                },
                ensure_ascii=False,
            )
        )
        return response


def _elapsed_ms(started_at: float) -> int:
    return max(0, round((perf_counter() - started_at) * 1000))
