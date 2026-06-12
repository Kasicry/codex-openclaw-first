[CmdletBinding()]
param(
    [string]$Database = "openclaw_news",
    [string]$DatabaseUser = "openclaw_app",
    [string]$BackendBaseUrl = "http://localhost:13510",
    [ValidateRange(1, 100)]
    [int]$SampleCount = 20
)

$ErrorActionPreference = "Stop"

function Assert-DatabaseName {
    param(
        [string]$Name,
        [string]$ParameterName
    )

    if ($Name -notmatch "^[a-z][a-z0-9_]{0,62}$") {
        throw "$ParameterName must be a lowercase PostgreSQL identifier."
    }
}

function Assert-LoopbackBaseUrl {
    param([string]$Value)

    $uri = [Uri]$Value
    if ($uri.Scheme -ne "http" -or $uri.Host -notin @("localhost", "127.0.0.1", "::1")) {
        throw "BackendBaseUrl must be an HTTP loopback URL."
    }

    return $uri.GetLeftPart([System.UriPartial]::Authority).TrimEnd("/")
}

function Get-Percentile {
    param(
        [double[]]$Values,
        [double]$Percentile
    )

    $sorted = @($Values | Sort-Object)
    if ($sorted.Count -eq 0) {
        return $null
    }

    $index = [Math]::Ceiling($Percentile * $sorted.Count) - 1
    return [Math]::Round($sorted[[Math]::Max(0, $index)], 3)
}

function Measure-LocalEndpoint {
    param(
        [string]$Name,
        [string]$Url
    )

    $headers = @{}
    if ($env:READ_API_KEY) {
        $headers["X-API-Key"] = $env:READ_API_KEY
    }

    $durations = @()
    for ($sample = 0; $sample -lt $SampleCount; $sample++) {
        $stopwatch = [System.Diagnostics.Stopwatch]::StartNew()
        $response = Invoke-WebRequest `
            -Uri $Url `
            -Headers $headers `
            -UseBasicParsing `
            -MaximumRedirection 0
        $stopwatch.Stop()

        if ($response.StatusCode -ne 200) {
            throw "Local endpoint returned HTTP $($response.StatusCode): $Name"
        }
        $durations += $stopwatch.Elapsed.TotalMilliseconds
    }

    return [ordered]@{
        sample_count = $durations.Count
        p50_ms       = Get-Percentile -Values $durations -Percentile 0.50
        p95_ms       = Get-Percentile -Values $durations -Percentile 0.95
        max_ms       = [Math]::Round(($durations | Measure-Object -Maximum).Maximum, 3)
    }
}

function Invoke-AggregateQuery {
    param([string]$Query)

    $result = & docker compose exec -T postgres psql `
        -U $DatabaseUser `
        -d $Database `
        -X `
        -q `
        -A `
        -t `
        -v ON_ERROR_STOP=1 `
        -c $Query

    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL aggregate query failed."
    }

    return (($result | Out-String).Trim() | ConvertFrom-Json)
}

Assert-DatabaseName -Name $Database -ParameterName "Database"
Assert-DatabaseName -Name $DatabaseUser -ParameterName "DatabaseUser"
$baseUrl = Assert-LoopbackBaseUrl -Value $BackendBaseUrl

$aggregateQuery = @"
BEGIN;
SET TRANSACTION READ ONLY;
WITH article_stats AS (
    SELECT
        count(*) AS total,
        count(*) FILTER (WHERE published_at >= now() - interval '24 hours') AS last_24h,
        count(*) FILTER (WHERE published_at >= now() - interval '7 days') AS last_7d,
        min(published_at) AS oldest_published_at,
        max(published_at) AS newest_published_at,
        count(*) FILTER (WHERE collection_status = 'COLLECTED') AS collected,
        count(*) FILTER (WHERE collection_status = 'SUMMARIZED') AS summarized,
        count(*) FILTER (WHERE collection_status = 'SUMMARY_FAILED') AS summary_failed,
        count(*) FILTER (WHERE notification_sent) AS notification_sent,
        count(*) FILTER (WHERE NOT notification_sent) AS notification_pending
    FROM news_articles
),
related_stats AS (
    SELECT count(*) AS multi_source_articles
    FROM (
        SELECT article_id
        FROM news_article_related_sources
        GROUP BY article_id
        HAVING count(*) > 1
    ) related
),
source_stats AS (
    SELECT
        count(*) FILTER (WHERE source = 'openai') AS openai,
        count(*) FILTER (WHERE source = 'anthropic') AS anthropic,
        count(*) FILTER (WHERE source = 'google-ai') AS google_ai,
        count(*) FILTER (WHERE source NOT IN ('openai', 'anthropic', 'google-ai')) AS other
    FROM news_articles
),
impact_stats AS (
    SELECT
        count(*) FILTER (WHERE impact = 'PENDING') AS pending,
        count(*) FILTER (WHERE impact = 'HIGH') AS high,
        count(*) FILTER (WHERE impact = 'MEDIUM') AS medium,
        count(*) FILTER (WHERE impact = 'LOW') AS low
    FROM news_articles
),
index_stats AS (
    SELECT
        count(*) AS tracked_indexes,
        COALESCE(sum(idx_scan), 0) AS total_index_scans
    FROM pg_stat_user_indexes
    WHERE schemaname = 'public'
),
flyway_stats AS (
    SELECT version
    FROM flyway_schema_history
    WHERE success
    ORDER BY installed_rank DESC
    LIMIT 1
)
SELECT json_build_object(
    'database_size_bytes', pg_database_size(current_database()),
    'flyway_version', (SELECT version FROM flyway_stats),
    'articles', json_build_object(
        'total', article_stats.total,
        'last_24h', article_stats.last_24h,
        'last_7d', article_stats.last_7d,
        'oldest_published_at', article_stats.oldest_published_at,
        'newest_published_at', article_stats.newest_published_at,
        'collection_status', json_build_object(
            'collected', article_stats.collected,
            'summarized', article_stats.summarized,
            'summary_failed', article_stats.summary_failed
        ),
        'summary_success_rate', CASE
            WHEN article_stats.summarized + article_stats.summary_failed = 0 THEN NULL
            ELSE round(
                article_stats.summarized::numeric
                / (article_stats.summarized + article_stats.summary_failed),
                4
            )
        END,
        'notification', json_build_object(
            'sent', article_stats.notification_sent,
            'pending', article_stats.notification_pending
        ),
        'multi_source_articles', related_stats.multi_source_articles
    ),
    'sources', json_build_object(
        'openai', source_stats.openai,
        'anthropic', source_stats.anthropic,
        'google_ai', source_stats.google_ai,
        'other', source_stats.other
    ),
    'impact', json_build_object(
        'pending', impact_stats.pending,
        'high', impact_stats.high,
        'medium', impact_stats.medium,
        'low', impact_stats.low
    ),
    'indexes', json_build_object(
        'tracked', index_stats.tracked_indexes,
        'total_scans', index_stats.total_index_scans
    )
)
FROM article_stats, related_stats, source_stats, impact_stats, index_stats;
ROLLBACK;
"@

$databaseSnapshot = Invoke-AggregateQuery -Query $aggregateQuery
$apiSnapshot = [ordered]@{
    latest = Measure-LocalEndpoint -Name "latest" -Url "$baseUrl/api/news/latest"
    today  = Measure-LocalEndpoint -Name "today" -Url "$baseUrl/api/news/today"
    query  = Measure-LocalEndpoint -Name "query" -Url "$baseUrl/api/news/query?size=20"
}

$snapshot = [ordered]@{
    schema_version      = 1
    captured_at         = [DateTime]::UtcNow.ToString("o")
    data_classification = "aggregate-only"
    database            = $databaseSnapshot
    local_api           = $apiSnapshot
}

$snapshot | ConvertTo-Json -Depth 10
