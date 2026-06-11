package com.kasicry.openclawnews.news;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface NewsArticleRepository extends JpaRepository<NewsArticle, Long> {

    boolean existsByUrl(String url);

    List<NewsArticle> findTop20ByOrderByPublishedAtDesc();

    List<NewsArticle> findByPublishedAtBetweenOrderByPublishedAtDesc(Instant from, Instant to);

    List<NewsArticle> findByTitleContainingIgnoreCaseOrderByPublishedAtDesc(String keyword);
}
