import re
from difflib import SequenceMatcher

from app.models import Article


def normalize_title(value: str) -> str:
    return re.sub(r"\s+", " ", re.sub(r"[^\w\s]", " ", value.casefold())).strip()


def deduplicate_articles(
    articles: list[Article],
    title_similarity_threshold: float = 0.92,
) -> tuple[list[Article], int]:
    representatives: list[Article] = []
    duplicate_count = 0

    for article in articles:
        duplicate_index = _find_duplicate(
            representatives,
            article,
            title_similarity_threshold,
        )
        if duplicate_index is None:
            representatives.append(article)
            continue

        representative = representatives[duplicate_index]
        related_sources = sorted(
            {
                representative.source,
                article.source,
                *representative.related_sources,
                *article.related_sources,
            }
        )
        representatives[duplicate_index] = representative.model_copy(
            update={"related_sources": related_sources}
        )
        duplicate_count += 1

    return representatives, duplicate_count


def _find_duplicate(
    representatives: list[Article],
    candidate: Article,
    title_similarity_threshold: float,
) -> int | None:
    candidate_title = normalize_title(candidate.title)
    for index, representative in enumerate(representatives):
        if str(representative.url) == str(candidate.url):
            return index

        similarity = SequenceMatcher(
            None,
            normalize_title(representative.title),
            candidate_title,
        ).ratio()
        if similarity >= title_similarity_threshold:
            return index
    return None
