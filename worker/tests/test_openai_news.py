from app.collectors.openai_news import OpenAINewsCollector

RSS_FIXTURE = """
<rss version="2.0">
  <channel>
    <item>
      <title>Codex release</title>
      <link>https://openai.com/index/codex-release/</link>
      <pubDate>Wed, 10 Jun 2026 10:00:00 GMT</pubDate>
      <description><![CDATA[<p>New coding agent capabilities.</p>]]></description>
    </item>
    <item>
      <title>Unrelated company update</title>
      <link>https://openai.com/index/company-update/</link>
      <description>General update.</description>
    </item>
  </channel>
</rss>
"""


def test_parse_filters_openai_rss_by_keyword() -> None:
    articles = OpenAINewsCollector.parse(RSS_FIXTURE, ["Codex"])

    assert len(articles) == 1
    assert articles[0].source == "openai"
    assert articles[0].title == "Codex release"
    assert articles[0].content == "New coding agent capabilities."
    assert articles[0].published_at is not None


def test_external_collection_is_disabled_by_default() -> None:
    collector = OpenAINewsCollector()

    try:
        collector(["AI"])
        raise AssertionError("collector should be disabled")
    except RuntimeError as exc:
        assert str(exc) == "external collection is disabled"
