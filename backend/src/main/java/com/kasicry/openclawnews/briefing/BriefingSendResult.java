package com.kasicry.openclawnews.briefing;

public class BriefingSendResult {

    private final int sentArticleCount;

    public BriefingSendResult(int sentArticleCount) {
        this.sentArticleCount = sentArticleCount;
    }

    public int getSentArticleCount() {
        return sentArticleCount;
    }
}
