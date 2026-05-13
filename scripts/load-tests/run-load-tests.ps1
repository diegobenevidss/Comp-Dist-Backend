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

if (-not (Test-Path $ResultsDir)) {
    New-Item -ItemType Directory -Path $ResultsDir | Out-Null
}

if (-not (Get-Command k6 -ErrorAction SilentlyContinue)) {
    Write-Error "k6 nao encontrado. Instale em: https://grafana.com/docs/k6/latest/set-up/install-k6/"
    exit 1
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

$TestFile = Join-Path $ScriptDir "load-test.js"
$Timestamp = Get-Date -Format "yyyyMMdd-HHmmss"

$K6Args = @(
    "run",
    "--out", "json=$ResultsDir/raw-$Timestamp.json"
)

if ($Scenario -ne "all") {
    $K6Args += @("--scenario", $Scenario)
}

$K6Args += $TestFile

Write-Host "Iniciando k6..." -ForegroundColor Yellow
k6 @K6Args

if ($LASTEXITCODE -ne 0) {
    Write-Host ""
    Write-Host "FALHA: Alguns thresholds nao foram atingidos (exit code $LASTEXITCODE)" -ForegroundColor Red
    Write-Host "Verifique o arquivo: $ResultsDir/summary.json" -ForegroundColor Yellow
    exit $LASTEXITCODE
}

Write-Host ""
Write-Host "Testes concluidos com sucesso!" -ForegroundColor Green
Write-Host "Resultados em: $ResultsDir" -ForegroundColor Green
