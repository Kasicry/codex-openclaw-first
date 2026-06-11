package com.kasicry.openclawnews.news;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

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
}
