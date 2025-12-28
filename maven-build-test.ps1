# Maven Build Test Script
# @AIGenerated("copilot")
# This script mimics the GitHub Actions workflow jobs: "build" and "test-compatibility"
# It builds the project with JDK 21 + SLF4J 2.0, then tests with all JDK/SLF4J combinations

param(
    [string]$ProjectPath = (Get-Location),
    [switch]$SkipBuild,
    [switch]$SkipTests,
    [switch]$SkipCache
)

# Define JDK paths
$jdks = @{
    "8"  = "C:\Users\dffwe\.jdks\temurin-1.8.0_442"
    "11" = "C:\Users\dffwe\.jdks\temurin-11.0.29"
    "17" = "C:\Users\dffwe\.jdks\temurin-17.0.17"
    "21" = "C:\Users\dffwe\.jdks\temurin-21.0.9"
}

# Configuration
$testMatrix = @(
    @{ JavaVersion = "8";  Profile = "slf4j-1.7-javax" }
    @{ JavaVersion = "8";  Profile = "slf4j-2.0-javax" }
    @{ JavaVersion = "11"; Profile = "slf4j-1.7-javax" }
    @{ JavaVersion = "11"; Profile = "slf4j-2.0-javax" }
    @{ JavaVersion = "11"; Profile = "slf4j-2.0" }
    @{ JavaVersion = "17"; Profile = "slf4j-1.7-javax" }
    @{ JavaVersion = "17"; Profile = "slf4j-2.0-javax" }
    @{ JavaVersion = "17"; Profile = "slf4j-2.0" }
    @{ JavaVersion = "21"; Profile = "slf4j-1.7-javax" }
    @{ JavaVersion = "21"; Profile = "slf4j-2.0-javax" }
    # Excluded: JDK 21 + SLF4J 2.0 (tested in build job)
    # Excluded: JDK 8 + SLF4J 2.0 (requires Java 11+)
)

# Results tracking
$results = @{
    BuildSuccess = $false
    BuildTime = 0
    TestResults = @()
    TotalTests = 0
    SuccessfulTests = 0
    FailedTests = @()
    SkippedTests = @()
}

# Color output functions
function Write-Header {
    param([string]$Message)
    Write-Host ""
    Write-Host "=" * 80 -ForegroundColor Cyan
    Write-Host $Message -ForegroundColor Cyan
    Write-Host "=" * 80 -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Green
}

function Write-Error {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Red
}

function Write-Warning {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Yellow
}

function Write-Info {
    param([string]$Message)
    Write-Host $Message -ForegroundColor Gray
}

# Validate JDK paths
Write-Header "Validating JDK Installations"
foreach ($version in $jdks.Keys | Sort-Object) {
    $javaPath = $jdks[$version]
    if (Test-Path "$javaPath\bin\java.exe") {
        Write-Success "✓ JDK $version found at: $javaPath"
    } else {
        Write-Error "✗ JDK $version NOT found at: $javaPath"
        exit 1
    }
}

# Change to project directory
Set-Location $ProjectPath
if (-not (Test-Path "pom.xml")) {
    Write-Error "Error: pom.xml not found in $ProjectPath"
    exit 1
}

# Job 1: Build with JDK 21 + SLF4J 2.0
if (-not $SkipBuild) {
    Write-Header "JOB 1: Build (JDK 21, SLF4J 2.0)"

    $buildStartTime = Get-Date

    # Set JAVA_HOME for JDK 21
    $env:JAVA_HOME = $jdks["21"]

    try {
        # Populate Maven cache
        if (-not $SkipCache) {
            Write-Info "Populating Maven cache..."
            & .\mvnw.cmd dependency:resolve-plugins --batch-mode
            & .\mvnw.cmd dependency:go-offline -P slf4j-2.0,javadoc-validation --batch-mode
            & .\mvnw.cmd dependency:resolve -P slf4j-1.7-javax --batch-mode
            & .\mvnw.cmd dependency:resolve -P slf4j-2.0-javax --batch-mode
            & .\mvnw.cmd dependency:resolve -P slf4j-1.7-javax,jdk-8 --batch-mode
            Write-Success "Maven cache populated"
        }

        # Build and run full test suite
        Write-Info "Building and running full test suite with SLF4J 2.0..."
        & .\mvnw.cmd clean verify -P slf4j-2.0,javadoc-validation --batch-mode

        if ($LASTEXITCODE -eq 0) {
            $results.BuildSuccess = $true
            Write-Success "✓ Build succeeded"
        } else {
            Write-Error "✗ Build failed with exit code $LASTEXITCODE"
            exit 1
        }
    } catch {
        Write-Error "✗ Build failed with exception: $_"
        exit 1
    } finally {
        $results.BuildTime = (Get-Date) - $buildStartTime
    }
} else {
    Write-Header "Build job SKIPPED (use -SkipBuild parameter)"
}

# Job 2: Test compatibility
if (-not $SkipTests) {
    Write-Header "JOB 2: Test Compatibility (All JDK/SLF4J combinations)"

    $results.TotalTests = $testMatrix.Count

    foreach ($testCase in $testMatrix) {
        $javaVersion = $testCase.JavaVersion
        $profile = $testCase.Profile

        Write-Info ""
        Write-Info "Testing: JDK $javaVersion + $profile"

        # Set JAVA_HOME
        $env:JAVA_HOME = $jdks[$javaVersion]

        # Verify java executable exists
        $javaExe = "$($env:JAVA_HOME)\bin\java.exe"
        if (-not (Test-Path $javaExe)) {
            Write-Error "✗ Java executable not found: $javaExe"
            $results.FailedTests += @{ JavaVersion = $javaVersion; Profile = $profile; Reason = "JAVA_HOME invalid" }
            continue
        }

        # Run tests
        $testStartTime = Get-Date

        try {
            & .\mvnw.cmd surefire:test@unit-tests surefire:test@logback-tests -P $profile --batch-mode -Dmaven.enforcer.skip=true

            if ($LASTEXITCODE -eq 0) {
                $testDuration = (Get-Date) - $testStartTime
                Write-Success "✓ JDK $javaVersion + $profile: PASSED (${testDuration.TotalSeconds:F1}s)"
                $results.SuccessfulTests++
                $results.TestResults += @{
                    JavaVersion = $javaVersion
                    Profile = $profile
                    Status = "PASSED"
                    Duration = $testDuration
                }
            } else {
                Write-Error "✗ JDK $javaVersion + $profile: FAILED"
                $results.FailedTests += @{ JavaVersion = $javaVersion; Profile = $profile; ExitCode = $LASTEXITCODE }
            }
        } catch {
            Write-Error "✗ JDK $javaVersion + $profile: EXCEPTION - $_"
            $results.FailedTests += @{ JavaVersion = $javaVersion; Profile = $profile; Reason = $_.Exception.Message }
        }
    }
} else {
    Write-Header "Test job SKIPPED (use -SkipTests parameter)"
}

# Print summary
Write-Header "SUMMARY"

Write-Host ""
Write-Host "Build Status:" -ForegroundColor Cyan
if ($results.BuildSuccess) {
    Write-Success "✓ Build completed successfully in $($results.BuildTime.TotalSeconds.ToString('F1'))s"
} else {
    Write-Warning "⊘ Build job skipped or not executed"
}

Write-Host ""
Write-Host "Test Results:" -ForegroundColor Cyan
if ($results.TotalTests -gt 0) {
    Write-Host "Total:      $($results.TotalTests) tests"
    Write-Success "✓ Passed:    $($results.SuccessfulTests) tests"

    if ($results.FailedTests.Count -gt 0) {
        Write-Error "✗ Failed:    $($results.FailedTests.Count) tests"
        Write-Host ""
        Write-Host "Failed Tests:" -ForegroundColor Red
        foreach ($failed in $results.FailedTests) {
            Write-Error "  - JDK $($failed.JavaVersion) + $($failed.Profile): $(if ($failed.ExitCode) { "Exit code $($failed.ExitCode)" } else { $failed.Reason })"
        }
    }

    Write-Host ""
    Write-Host "Test Details:" -ForegroundColor Cyan
    foreach ($test in $results.TestResults | Where-Object { $_.Status -eq "PASSED" }) {
        Write-Success "  ✓ JDK $($test.JavaVersion) + $($test.Profile): $($test.Duration.TotalSeconds.ToString('F1'))s"
    }
} else {
    Write-Warning "⊘ No tests executed"
}

Write-Host ""
Write-Host "=" * 80 -ForegroundColor Cyan

# Exit with appropriate code
if ($results.FailedTests.Count -gt 0) {
    Write-Error "Overall result: FAILED"
    exit 1
} else {
    Write-Success "Overall result: SUCCESS"
    exit 0
}

