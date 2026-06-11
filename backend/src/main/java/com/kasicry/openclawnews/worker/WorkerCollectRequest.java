package com.kasicry.openclawnews.worker;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class WorkerCollectRequest {

    @NotEmpty
    private List<String> sources = new ArrayList<String>();

    private List<String> keywords = new ArrayList<String>();

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }
}
