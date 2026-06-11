package com.kasicry.openclawnews.news;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/news")
public class NewsQueryController {

    private final NewsArticleRepository repository;

    public NewsQueryController(NewsArticleRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/latest")
    public List<NewsArticle> latest() {
        return repository.findTop20ByOrderByPublishedAtDesc();
    }

    @GetMapping("/today")
    public List<NewsArticle> today() {
        Instant from = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        return repository.findByPublishedAtBetweenOrderByPublishedAtDesc(
                from,
                from.plusSeconds(24 * 60 * 60)
        );
    }

    @GetMapping("/search")
    public List<NewsArticle> search(
            @RequestParam @NotBlank @Size(max = 100) String keyword
    ) {
        return repository.findByTitleContainingIgnoreCaseOrderByPublishedAtDesc(keyword);
    }
}
