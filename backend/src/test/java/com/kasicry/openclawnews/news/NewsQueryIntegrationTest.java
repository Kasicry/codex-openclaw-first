package com.kasicry.openclawnews.news;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@Transactional
class NewsQueryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NewsArticleRepository repository;

    @Test
    void queryFiltersAndSerializesProcessingMetadata() throws Exception {
        NewsArticle article = new NewsArticle(
                "openai",
                "Codex release",
                "https://example.com/integration/codex",
                Instant.parse("2026-06-11T00:00:00Z"),
                "Release details",
                Arrays.asList("Codex", "Developer"),
                Arrays.asList("openai", "example")
        );
        article.applySummary("Core summary", NewsImpact.HIGH, "Review the workflow");
        repository.saveAndFlush(article);

        mockMvc.perform(get("/api/news/query")
                        .param("impact", "high")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].summary").value("Core summary"))
                .andExpect(jsonPath("$.content[0].impact").value("HIGH"))
                .andExpect(jsonPath("$.content[0].collection_status").value("SUMMARIZED"))
                .andExpect(jsonPath("$.content[0].keywords").isArray())
                .andExpect(jsonPath("$.content[0].related_sources").isArray());
    }
}
