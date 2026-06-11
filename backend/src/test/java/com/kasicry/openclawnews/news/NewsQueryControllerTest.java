package com.kasicry.openclawnews.news;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NewsQueryController.class)
class NewsQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NewsArticleRepository repository;

    @Test
    void latestReturnsReadOnlyNewsResult() throws Exception {
        when(repository.findTop20ByOrderByPublishedAtDesc()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/news/latest"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void searchRejectsBlankKeyword() throws Exception {
        mockMvc.perform(get("/api/news/search").param("keyword", " "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void searchRejectsMissingKeywordWithConsistentError() throws Exception {
        mockMvc.perform(get("/api/news/search"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void queryReturnsPagedReadOnlyResult() throws Exception {
        when(repository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/api/news/query")
                        .param("keyword", "Codex")
                        .param("source", "OpenAI")
                        .param("impact", "high")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.total_elements").value(0));
    }

    @Test
    void queryRejectsInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/news/query")
                        .param("from", "2026-06-12T10:00:00Z")
                        .param("to", "2026-06-12T09:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.message").value("from must be before or equal to to"));
    }

    @Test
    void queryRejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/news/query").param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void queryRejectsInvalidDateFormatWithConsistentError() throws Exception {
        mockMvc.perform(get("/api/news/query").param("from", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void queryRejectsUnknownImpact() throws Exception {
        mockMvc.perform(get("/api/news/query").param("impact", "critical"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
