from app.collectors.anthropic_news import AnthropicNewsCollector


def test_parse_anthropic_news_filters_and_deduplicates() -> None:
    html = """
    <main>
      <a href="/news/claude-release">
        <time datetime="2026-06-11T00:00:00Z">Jun 11, 2026</time>
        <h3>Claude release</h3>
        <p>New AI coding capabilities.</p>
      </a>
      <a href="/news/claude-release"><h3>Claude release</h3></a>
      <a href="/news/company-update"><h3>Company update</h3></a>
    </main>
    """

    articles = AnthropicNewsCollector.parse(html, ["Claude"])

    assert len(articles) == 1
    assert articles[0].source == "anthropic"
    assert articles[0].title == "Claude release"
    assert str(articles[0].url) == "https://www.anthropic.com/news/claude-release"
    assert articles[0].published_at is not None
