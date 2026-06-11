import os

from fastapi import FastAPI, HTTPException

from app.collectors.anthropic_news import AnthropicNewsCollector
from app.collectors.google_ai import GoogleAINewsCollector
from app.collectors.openai_news import OpenAINewsCollector
from app.models import CollectRequest, CollectResponse, SummarizeRequest, SummaryResult
from app.services.collector import CollectionCoordinator
from app.services.summarizer import OpenAISummarizer

app = FastAPI(title="IT News Crawling Worker", version="0.1.0")
external_collection_enabled = os.getenv("COLLECT_EXTERNAL_ENABLED", "false").lower() == "true"
ai_summary_enabled = os.getenv("AI_SUMMARY_ENABLED", "false").lower() == "true"
coordinator = CollectionCoordinator(
    {
        "anthropic": AnthropicNewsCollector(enabled=external_collection_enabled),
        "google-ai": GoogleAINewsCollector(enabled=external_collection_enabled),
        "openai": OpenAINewsCollector(enabled=external_collection_enabled),
    }
)
summarizer = OpenAISummarizer(
    enabled=ai_summary_enabled,
    api_key=os.getenv("OPENAI_API_KEY"),
    model=os.getenv("OPENAI_MODEL"),
)


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "up"}


@app.post("/v1/collect", response_model=CollectResponse)
def collect(request: CollectRequest) -> CollectResponse:
    return coordinator.collect(request.sources, request.keywords)


@app.post("/v1/summarize", response_model=SummaryResult)
def summarize(request: SummarizeRequest) -> SummaryResult:
    try:
        return summarizer.summarize(request)
    except PermissionError as exc:
        raise HTTPException(status_code=403, detail=str(exc)) from exc
