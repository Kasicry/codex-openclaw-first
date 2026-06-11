package com.kasicry.openclawnews.worker;

public class WorkerSummaryResponse {

    private String title;
    private String coreContent;
    private String impact;
    private String developerView;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCoreContent() {
        return coreContent;
    }

    public void setCoreContent(String coreContent) {
        this.coreContent = coreContent;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getDeveloperView() {
        return developerView;
    }

    public void setDeveloperView(String developerView) {
        this.developerView = developerView;
    }
}
