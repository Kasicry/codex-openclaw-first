package com.kasicry.openclawnews.operations;

public enum OperationalAlertEvent {
    COLLECTION_PARTIAL("warning", "collection", "collection.partial"),
    COLLECTION_FAILURE("warning", "collection", "collection.failure"),
    SUMMARY_FAILURE("warning", "summary", "summary.failure"),
    BRIEFING_FAILURE("critical", "briefing", "briefing.failure");

    private final String severity;
    private final String component;
    private final String code;

    OperationalAlertEvent(String severity, String component, String code) {
        this.severity = severity;
        this.component = component;
        this.code = code;
    }

    public String getSeverity() {
        return severity;
    }

    public String getComponent() {
        return component;
    }

    public String getCode() {
        return code;
    }
}
