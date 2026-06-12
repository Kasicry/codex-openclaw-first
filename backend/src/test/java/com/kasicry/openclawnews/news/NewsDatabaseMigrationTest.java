package com.kasicry.openclawnews.news;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class NewsDatabaseMigrationTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void createsIndexesForFrequentReadQueries() throws Exception {
        Set<String> indexNames = new HashSet<String>();

        try (Connection connection = dataSource.getConnection();
             ResultSet indexes = connection.getMetaData().getIndexInfo(
                     null,
                     null,
                     "NEWS_ARTICLES",
                     false,
                     false
             )) {
            while (indexes.next()) {
                String indexName = indexes.getString("INDEX_NAME");
                if (indexName != null) {
                    indexNames.add(indexName.toLowerCase(Locale.ROOT));
                }
            }
        }

        assertThat(indexNames).contains(
                "idx_news_articles_published_at",
                "idx_news_articles_source_published_at",
                "idx_news_articles_impact_published_at",
                "idx_news_articles_notification_published_at"
        );
    }
}
