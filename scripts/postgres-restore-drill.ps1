[CmdletBinding()]
param(
    [string]$SourceDatabase = "openclaw_news",
    [string]$RestoreDatabase = "openclaw_news_restore_drill",
    [string]$DatabaseUser = "openclaw_app"
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

function Invoke-PostgresTool {
    param([string[]]$Arguments)

    & docker compose exec -T postgres @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL tool failed: $($Arguments[0])"
    }
}

function Invoke-ScalarQuery {
    param(
        [string]$Database,
        [string]$Query
    )

    $result = & docker compose exec -T postgres psql `
        -U $DatabaseUser `
        -d $Database `
        -X `
        -A `
        -t `
        -v ON_ERROR_STOP=1 `
        -c $Query

    if ($LASTEXITCODE -ne 0) {
        throw "PostgreSQL query failed for database: $Database"
    }

    return ($result | Out-String).Trim()
}

Assert-DatabaseName -Name $SourceDatabase -ParameterName "SourceDatabase"
Assert-DatabaseName -Name $RestoreDatabase -ParameterName "RestoreDatabase"
Assert-DatabaseName -Name $DatabaseUser -ParameterName "DatabaseUser"

if ($SourceDatabase -eq $RestoreDatabase) {
    throw "RestoreDatabase must differ from SourceDatabase."
}
if (-not $RestoreDatabase.EndsWith("_restore_drill")) {
    throw "RestoreDatabase must end with _restore_drill."
}

$existingRestoreDatabase = Invoke-ScalarQuery `
    -Database $SourceDatabase `
    -Query "SELECT count(*) FROM pg_database WHERE datname = '$RestoreDatabase';"
if ($existingRestoreDatabase -ne "0") {
    throw "RestoreDatabase already exists. Refusing to modify it."
}

$dumpPath = "/tmp/openclaw_restore_drill_$([System.Diagnostics.Process]::GetCurrentProcess().Id).dump"
$restoreCreated = $false

$fingerprintQuery = @"
SELECT
    (SELECT count(*) FROM news_articles)::text || '|' ||
    (SELECT count(*) FROM news_article_keywords)::text || '|' ||
    (SELECT count(*) FROM news_article_related_sources)::text || '|' ||
    COALESCE((SELECT max(published_at)::text FROM news_articles), '') || '|' ||
    COALESCE((
        SELECT version
        FROM flyway_schema_history
        WHERE success
        ORDER BY installed_rank DESC
        LIMIT 1
    ), '');
"@

try {
    $sourceFingerprint = Invoke-ScalarQuery -Database $SourceDatabase -Query $fingerprintQuery

    Invoke-PostgresTool @(
        "pg_dump",
        "-U", $DatabaseUser,
        "-d", $SourceDatabase,
        "--format=custom",
        "--no-owner",
        "--no-privileges",
        "--file=$dumpPath"
    )

    Invoke-PostgresTool @(
        "createdb",
        "-U", $DatabaseUser,
        "--maintenance-db=$SourceDatabase",
        $RestoreDatabase
    )
    $restoreCreated = $true

    Invoke-PostgresTool @(
        "pg_restore",
        "-U", $DatabaseUser,
        "-d", $RestoreDatabase,
        "--exit-on-error",
        "--no-owner",
        "--no-privileges",
        $dumpPath
    )

    $restoreFingerprint = Invoke-ScalarQuery -Database $RestoreDatabase -Query $fingerprintQuery
    if ($sourceFingerprint -ne $restoreFingerprint) {
        throw "Restore verification failed. Source and restore fingerprints differ."
    }

    Write-Output "restore_drill=success"
    Write-Output "source_database=$SourceDatabase"
    Write-Output "restore_database=$RestoreDatabase"
    Write-Output "fingerprint=$sourceFingerprint"
}
finally {
    if ($restoreCreated) {
        Invoke-PostgresTool @(
            "dropdb",
            "-U", $DatabaseUser,
            "--maintenance-db=$SourceDatabase",
            "--if-exists",
            $RestoreDatabase
        )
    }
    Invoke-PostgresTool @(
        "rm",
        "-f",
        $dumpPath
    )
}
