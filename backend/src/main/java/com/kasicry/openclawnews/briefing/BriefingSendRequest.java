package com.kasicry.openclawnews.briefing;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class BriefingSendRequest {

    @NotBlank
    @Size(max = 4000)
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
