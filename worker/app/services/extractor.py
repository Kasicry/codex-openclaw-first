import re

from bs4 import BeautifulSoup


def compact_text(value: str) -> str:
    return re.sub(r"\s+", " ", value).strip()


def extract_article_text(html: str) -> tuple[str | None, str]:
    soup = BeautifulSoup(html, "html.parser")

    for tag in soup(["script", "style", "noscript", "svg"]):
        tag.decompose()

    title = compact_text(soup.title.get_text()) if soup.title else None
    main = soup.find("article") or soup.find("main") or soup.body or soup
    return title, compact_text(main.get_text(" "))
