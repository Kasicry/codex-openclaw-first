from app.models import Article
from app.services.collector import CollectionCoordinator


def test_collection_continues_when_one_source_fails() -> None:
    def successful_collector(keywords: list[str]) -> list[Article]:
        assert keywords == ["AI"]
        return [
            Article(
                source="working",
                title="AI release",
                url="https://example.com/news",
                content="release details",
            )
        ]

    def failed_collector(keywords: list[str]) -> list[Article]:
        raise RuntimeError("source unavailable")

    coordinator = CollectionCoordinator(
        {"working": successful_collector, "failed": failed_collector}
    )

    result = coordinator.collect(["failed", "working"], ["AI"])

    assert len(result.articles) == 1
    assert result.articles[0].matched_keywords == ["AI"]
    assert result.sources[0].status == "failed"
    assert result.sources[1].status == "completed"
    assert result.duration_ms >= 0


def test_collection_normalizes_and_deduplicates_urls() -> None:
    def first_collector(keywords: list[str]) -> list[Article]:
        return [
            Article(
                source="first",
                title="First",
                url="https://EXAMPLE.com/news/?utm_source=test&id=1#section",
            )
        ]

    def second_collector(keywords: list[str]) -> list[Article]:
        return [
            Article(
                source="second",
                title="Second",
                url="https://example.com/news?id=1",
            )
        ]

    result = CollectionCoordinator(
        {"first": first_collector, "second": second_collector}
    ).collect(["first", "second"], [])

    assert len(result.articles) == 1
    assert str(result.articles[0].url) == "https://example.com/news?id=1"
    assert result.duplicate_count == 1


def test_collection_records_only_keywords_present_in_article() -> None:
    def collector(keywords: list[str]) -> list[Article]:
        return [
            Article(
                source="source",
                title="Codex update",
                url="https://example.com/codex",
                content="Developer tooling release",
            )
        ]

    result = CollectionCoordinator({"source": collector}).collect(
        ["source"],
        ["AI", "Codex", "Developer"],
    )

    assert result.articles[0].matched_keywords == ["Codex", "Developer"]
