#Requires -Version 5.1
<#
.SYNOPSIS
    Executa testes de carga do HealthSys com k6.
.PARAMETER BaseUrl
    URL base da API (default: http://localhost:8080)
.PARAMETER AdminEmail
    Email do administrador (default: admin@healthsys.local)
.PARAMETER AdminPassword
    Senha do administrador (default: Admin@123)
.PARAMETER Scenario
    Cenário de teste: smoke | load | spike | all (default: all)
#>
param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$AdminEmail = "admin@healthsys.local",
    [string]$AdminPassword = "Admin@123",
    [ValidateSet("smoke", "load", "spike", "all")]
    [string]$Scenario = "all"
)

$ErrorActionPreference = "Stop"
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$ResultsDir = Join-Path $ScriptDir "results"
$RepoRoot = Resolve-Path (Join-Path $ScriptDir "..\..")

if (-not (Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir | Out-Null
}

Write-Host ""
Write-Host "=== HealthSys Load Tests ===" -ForegroundColor Cyan
Write-Host "Base URL:  $BaseUrl"
Write-Host "Scenario:  $Scenario"
Write-Host "Results:   $ResultsDir"
Write-Host ""

$env:BASE_URL = $BaseUrl
$env:ADMIN_EMAIL = $AdminEmail
$env:ADMIN_PASSWORD = $AdminPassword
$env:SCENARIO = $Scenario

$TestFile = Join-Path $ScriptDir "load-test.js"
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

$K6Args = @(
    "run",
    "--out", "json=$ResultsDir/raw-$Timestamp.json"
)

$K6Args += $TestFile

if (Get-Command k6 -ErrorAction SilentlyContinue) {
    Write-Host "Iniciando k6 local..." -ForegroundColor Yellow
    k6 @K6Args
} elseif (Get-Command docker -ErrorAction SilentlyContinue) {
    Write-Host "k6 local nao encontrado. Iniciando via Docker..." -ForegroundColor Yellow

    $DockerBaseUrl = $BaseUrl `
        -replace '^http://localhost:', 'http://host.docker.internal:' `
        -replace '^http://127\.0\.0\.1:', 'http://host.docker.internal:'

    $DockerArgs = @(
        "run",
        "--rm",
        "-v", "$($RepoRoot.Path):/workspace",
        "-w", "/workspace",
        "-e", "BASE_URL=$DockerBaseUrl",
        "-e", "ADMIN_EMAIL=$AdminEmail",
        "-e", "ADMIN_PASSWORD=$AdminPassword",
        "-e", "SCENARIO=$Scenario",
        "grafana/k6:0.50.0",
        "run",
        "--out", "json=/workspace/scripts/load-tests/results/raw-$Timestamp.json"
    )

    $DockerArgs += "/workspace/scripts/load-tests/load-test.js"
    docker @DockerArgs
} else {
    Write-Error "k6 e docker nao encontrados. Instale k6 ou Docker para executar os testes de carga."
    exit 1
}

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "FALHA: Alguns thresholds nao foram atingidos (exit code $LASTEXITCODE)" -ForegroundColor Red
    Write-Host "Verifique o arquivo: $ResultsDir/summary.json" -ForegroundColor Yellow
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Testes concluidos com sucesso!" -ForegroundColor Green
Write-Host "Resultados em: $ResultsDir" -ForegroundColor Green
