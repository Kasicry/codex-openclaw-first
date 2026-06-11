from datetime import datetime
from typing import Literal

from pydantic import BaseModel, Field, HttpUrl


class CollectRequest(BaseModel):
    sources: list[str] = Field(min_length=1)
    keywords: list[str] = Field(default_factory=list)


class Article(BaseModel):
    source: str
    title: str
    url: HttpUrl
    published_at: datetime | None = None
    content: str = ""
    related_sources: list[str] = Field(default_factory=list)


class SourceResult(BaseModel):
    source: str
    status: Literal["completed", "failed"]
    article_count: int = 0
    duration_ms: int = 0
    error: str | None = None


class CollectResponse(BaseModel):
    articles: list[Article]
    sources: list[SourceResult]
    duplicate_count: int = 0
    duration_ms: int = 0


class SummarizeRequest(BaseModel):
    title: str
    content: str = Field(min_length=1)


class SummaryResult(BaseModel):
    title: str
    core_content: str
    impact: Literal["high", "medium", "low"]
    developer_view: str
