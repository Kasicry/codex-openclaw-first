from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health() -> None:
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "up"}


def test_unknown_collector_is_reported_without_server_failure() -> None:
    response = client.post("/v1/collect", json={"sources": ["unknown"], "keywords": ["AI"]})

    assert response.status_code == 200
    assert response.json()["sources"][0]["status"] == "failed"


def test_summary_is_forbidden_by_default() -> None:
    response = client.post("/v1/summarize", json={"title": "News", "content": "Content"})

    assert response.status_code == 403
