from app.models import Article
from app.services.deduplicator import deduplicate_articles


def test_similar_titles_are_grouped_and_sources_are_preserved() -> None:
    articles = [
        Article(
            source="openai",
            title="OpenAI releases Codex update",
            url="https://openai.com/codex-update",
        ),
        Article(
            source="example",
            title="OpenAI releases Codex update!",
            url="https://example.com/openai-codex-update",
        ),
    ]

    representatives, duplicate_count = deduplicate_articles(articles)

    assert len(representatives) == 1
    assert duplicate_count == 1
    assert representatives[0].source == "openai"
    assert representatives[0].related_sources == ["example", "openai"]


def test_distinct_titles_are_not_grouped() -> None:
    articles = [
        Article(source="one", title="Codex release", url="https://example.com/one"),
        Article(source="two", title="PostgreSQL release", url="https://example.com/two"),
    ]

    representatives, duplicate_count = deduplicate_articles(articles)

    assert len(representatives) == 2
    assert duplicate_count == 0
