$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$settings = Join-Path $projectRoot '.env'
if (-not (Test-Path -LiteralPath $settings)) {
    throw 'Copy .env.example to .env and configure PostgreSQL and JWT_SECRET first.'
}
foreach ($line in Get-Content -LiteralPath $settings) {
    if ($line -match '^\s*([A-Z_][A-Z0-9_]*)=(.*)$') {
        [Environment]::SetEnvironmentVariable($Matches[1], $Matches[2].Trim(), 'Process')
    }
}
if ($env:JWT_SECRET -like 'replace-*' -or $env:DB_PASSWORD -like 'replace-*') {
    throw 'Replace the sample secrets in .env with your own random values.'
}
Push-Location (Join-Path $projectRoot 'backend')
try { mvn.cmd spring-boot:run } finally { Pop-Location }
