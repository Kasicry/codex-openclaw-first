package com.kasicry.openclawnews.news;

import com.kasicry.openclawnews.config.InvalidRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

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

    @GetMapping("/query")
    public Page<NewsArticle> query(
            @RequestParam(required = false)
            @Pattern(regexp = ".*\\S.*") @Size(max = 100) String keyword,
            @RequestParam(required = false)
            @Pattern(regexp = ".*\\S.*") @Size(max = 120) String source,
            @RequestParam(required = false)
            @Pattern(regexp = "(?i)high|medium|low") String impact,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidRequestException("from must be before or equal to to");
        }

        Specification<NewsArticle> specification = Specification.where(null);
        if (keyword != null) {
            String pattern = "%" + keyword.toLowerCase(Locale.ROOT) + "%";
            specification = specification.and((root, query, builder) ->
                    builder.like(builder.lower(root.get("title")), pattern));
        }
        if (source != null) {
            specification = specification.and((root, query, builder) ->
                    builder.equal(builder.lower(root.get("source")), source.toLowerCase(Locale.ROOT)));
        }
        if (impact != null) {
            NewsImpact parsedImpact = NewsImpact.valueOf(impact.toUpperCase(Locale.ROOT));
            specification = specification.and((root, query, builder) ->
                    builder.equal(root.get("impact"), parsedImpact));
        }
        if (from != null) {
            specification = specification.and((root, query, builder) ->
                    builder.greaterThanOrEqualTo(root.get("publishedAt"), from));
        }
        if (to != null) {
            specification = specification.and((root, query, builder) ->
                    builder.lessThanOrEqualTo(root.get("publishedAt"), to));
        }

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "publishedAt")
        );
        return repository.findAll(specification, pageRequest);
    }
}
