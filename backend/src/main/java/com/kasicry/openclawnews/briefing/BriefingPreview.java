package com.kasicry.openclawnews.briefing;

import java.time.LocalDate;

public class BriefingPreview {

    private final LocalDate date;
    private final int articleCount;
    private final String text;

    public BriefingPreview(LocalDate date, int articleCount, String text) {
        this.date = date;
        this.articleCount = articleCount;
        this.text = text;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getArticleCount() {
        return articleCount;
    }

    public String getText() {
        return text;
    }
}
