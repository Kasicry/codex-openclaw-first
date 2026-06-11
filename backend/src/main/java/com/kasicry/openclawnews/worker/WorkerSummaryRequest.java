package com.kasicry.openclawnews.worker;

public class WorkerSummaryRequest {

    private String title;
    private String content;

    public WorkerSummaryRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
