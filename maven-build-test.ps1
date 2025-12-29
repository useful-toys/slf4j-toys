# Maven Build Test Script
# @AIGenerated("copilot")

param(
    [string]$ProjectPath = (Get-Location),
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$SkipCache
)

$jdks = @{
    "8"  = "C:\Users\dffwe\.jdks\temurin-1.8.0_442"
    "11" = "C:\Users\dffwe\.jdks\temurin-11.0.29"
    "17" = "C:\Users\dffwe\.jdks\temurin-17.0.17"
    "21" = "C:\Users\dffwe\.jdks\temurin-21.0.9"
}

$testMatrix = @(
    @{ JavaVersion = "8";  Profile = "slf4j-1.7-javax" },
    @{ JavaVersion = "8";  Profile = "slf4j-2.0-javax" },
    @{ JavaVersion = "11"; Profile = "slf4j-1.7-javax" },
    @{ JavaVersion = "11"; Profile = "slf4j-2.0-javax" },
    @{ JavaVersion = "11"; Profile = "slf4j-2.0" },
    @{ JavaVersion = "17"; Profile = "slf4j-1.7-javax" },
    @{ JavaVersion = "17"; Profile = "slf4j-2.0-javax" },
    @{ JavaVersion = "17"; Profile = "slf4j-2.0" },
    @{ JavaVersion = "21"; Profile = "slf4j-1.7-javax" },
    @{ JavaVersion = "21"; Profile = "slf4j-2.0-javax" }
)

$buildSuccess = $false
$buildTime = 0
$successCount = 0
$failedTests = @()

function Print-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host ("=" * 80) -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host ("=" * 80) -ForegroundColor Cyan
}

function Print-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Print-Error {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}

function Print-Warning {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Yellow
}

function Print-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Gray
}

# Validate JDKs
Print-Header "Validating JDK Installations"
foreach ($ver in $jdks.Keys | Sort-Object) {
    $path = $jdks[$ver]
    if (Test-Path "$path\bin\java.exe") {
        Print-Success "O JDK $ver encontrado"
    } else {
        Print-Error "X JDK $ver NAO encontrado: $path"
        exit 1
    }
}

Set-Location $ProjectPath
if (-not (Test-Path "pom.xml")) {
    Print-Error "Erro: pom.xml nao encontrado"
    exit 1
}

# Build Job
if (-not $SkipBuild) {
    Print-Header "JOB 1: Build (JDK 21, SLF4J 2.0)"

    $buildStart = Get-Date
    $env:JAVA_HOME = $jdks["21"]

    if (-not $SkipCache) {
        Print-Info "Preenchendo cache Maven..."
        & .\mvnw.cmd dependency:resolve-plugins --batch-mode
        & .\mvnw.cmd dependency:go-offline -P slf4j-2.0,javadoc-validation --batch-mode
        & .\mvnw.cmd dependency:resolve -P slf4j-1.7-javax --batch-mode
        & .\mvnw.cmd dependency:resolve -P slf4j-2.0-javax --batch-mode
        & .\mvnw.cmd dependency:resolve -P slf4j-1.7-javax,jdk-8 --batch-mode
        Print-Success "O Cache Maven preenchido"
    }

    Print-Info "Compilando com SLF4J 2.0..."
    & .\mvnw.cmd clean verify -P slf4j-2.0,javadoc-validation --batch-mode
    $buildExitCode = $LASTEXITCODE

    if ($buildExitCode -eq 0) {
        $buildSuccess = $true
        Print-Success "O Build sucedido"
    } else {
        Print-Error "X Build falhou com código: $buildExitCode"
        exit 1
    }

    $buildTime = (Get-Date) - $buildStart
}

# Test Compatibility Job
if (-not $SkipTests) {
    Print-Header "JOB 2: Testes de Compatibilidade"

    foreach ($test in $testMatrix) {
        $javaVer = $test.JavaVersion
        $profile = $test.Profile

        Print-Info "Testando: JDK $javaVer + $profile"

        $env:JAVA_HOME = $jdks[$javaVer]

        $javaExe = "$($env:JAVA_HOME)\bin\java.exe"
        if (-not (Test-Path $javaExe)) {
            Print-Error "X Java nao encontrado para JDK $javaVer"
            $failedTests += "$javaVer/$profile"
            continue
        }

        $testStart = Get-Date
        & .\mvnw.cmd surefire:test@unit-tests surefire:test@logback-tests -P $profile --batch-mode "-Dmaven.enforcer.skip=true"
        $exitCode = $LASTEXITCODE

        if ($exitCode -eq 0) {
            $duration = (Get-Date) - $testStart
            $sec = [math]::Round($duration.TotalSeconds, 1)
            Print-Success "O JDK $javaVer + ${profile}: PASSOU ($sec`s)"
            $successCount++
        } else {
            Print-Error "X JDK $javaVer + ${profile}: FALHOU (código: $exitCode)"
            $failedTests += "$javaVer/$profile"
        }
    }
}

# Summary
Print-Header "RESUMO"

Write-Host ""
Write-Host "Status do Build:" -ForegroundColor Cyan
if ($buildSuccess) {
    $buildSec = [math]::Round($buildTime.TotalSeconds, 1)
    Print-Success "O Build completado em $buildSec`s"
} else {
    Print-Warning "O Build job ignorado"
}

Write-Host ""
Write-Host "Resultados dos Testes:" -ForegroundColor Cyan
if ($testMatrix.Count -gt 0) {
    Write-Host "Total:      $($testMatrix.Count) testes"
    Print-Success "O Passaram: $successCount testes"

    if ($failedTests.Count -gt 0) {
        Print-Error "X Falharam: $($failedTests.Count) testes"
        Write-Host ""
        Write-Host "Testes que Falharam:" -ForegroundColor Red
        foreach ($failed in $failedTests) {
            Print-Error "  - $failed"
        }
    }
}

Write-Host ""
Write-Host ("=" * 80) -ForegroundColor Cyan

if ($failedTests.Count -gt 0) {
    Print-Error "Resultado geral: FALHOU"
    exit 1
} else {
    Print-Success "Resultado geral: SUCESSO"
    exit 0
}

