from app.services.extractor import extract_article_text


def test_extract_article_text_uses_article_and_removes_script() -> None:
    html = """
    <html>
      <head><title> Example News </title></head>
      <body>
        <main>ignored</main>
        <article><h1>Headline</h1><p>Useful text</p><script>secret()</script></article>
      </body>
    </html>
    """

    title, text = extract_article_text(html)

    assert title == "Example News"
    assert text == "Headline Useful text"
