from app.collectors.google_ai import GoogleAINewsCollector


def test_google_ai_external_collection_is_disabled_by_default() -> None:
    collector = GoogleAINewsCollector()

    try:
        collector(["AI"])
        raise AssertionError("collector should be disabled")
    except RuntimeError as exc:
        assert str(exc) == "external collection is disabled"
