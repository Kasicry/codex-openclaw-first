package com.kasicry.openclawnews.worker;

import com.kasicry.openclawnews.news.NewsArticle;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/news")
public class SummaryController {

    private final SummaryService summaryService;
    private final boolean summaryEnabled;

    public SummaryController(
            SummaryService summaryService,
            @Value("${operations.summary-enabled:false}") boolean summaryEnabled
    ) {
        this.summaryService = summaryService;
        this.summaryEnabled = summaryEnabled;
    }

    @PostMapping("/{articleId}/summarize")
    public NewsArticle summarize(@PathVariable long articleId) {
        if (!summaryEnabled) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Summary operation requires explicit approval and configuration"
            );
        }
        return summaryService.summarize(articleId);
    }
}
