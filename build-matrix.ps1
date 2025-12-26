# Build Matrix Script - Mimics GitHub Actions workflow matrix testing
# This script replicates the maven-build-test.yml workflow locally

# Define available JDK paths
$jdks = @{
    "8"  = "C:\Users\dffwe\.jdks\temurin-1.8.0_442"
    "11" = "C:\Users\dffwe\.jdks\temurin-11.0.29"
    "17" = "C:\Users\dffwe\.jdks\temurin-17.0.17"
    "21" = "C:\Users\dffwe\.jdks\temurin-21.0.9"
}

# Define test matrix (JDK version, SLF4J profile)
$testMatrix = @(
    @{JDK = "8";  SLF4J = "slf4j-1.7"},
    @{JDK = "8";  SLF4J = "slf4j-2.0"},
    @{JDK = "11"; SLF4J = "slf4j-1.7"},
    @{JDK = "11"; SLF4J = "slf4j-2.0"},
    @{JDK = "17"; SLF4J = "slf4j-1.7"},
    @{JDK = "17"; SLF4J = "slf4j-2.0"},
    @{JDK = "21"; SLF4J = "slf4j-1.7"}
)

# Store results
$results = @()

# Initialize counters
$totalBuilds = 0
$successCount = 0
$failureCount = 0

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Build Matrix - GitHub Actions Simulation" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# ============================================
# JOB 1: Main Build (JDK 21, SLF4J 2.0)
# ============================================
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "JOB 1: Main Build" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "Configuration: JDK 21 + SLF4J 2.0 + javadoc-validation" -ForegroundColor Gray
Write-Host "Command: clean verify -P slf4j-2.0,javadoc-validation`n" -ForegroundColor Gray

$mainBuildJdk = "21"
$mainBuildJdkPath = $jdks[$mainBuildJdk]

# Verify JDK exists
if (-not (Test-Path $mainBuildJdkPath)) {
    Write-Host "ERROR: JDK $mainBuildJdk not found at $mainBuildJdkPath" -ForegroundColor Red
    Write-Host "Cannot proceed without main build JDK" -ForegroundColor Red
    exit 1
}

# Set JAVA_HOME
$env:JAVA_HOME = $mainBuildJdkPath

# Display Java version
$javaExe = Join-Path $mainBuildJdkPath "bin\java.exe"
Write-Host "Java version:" -ForegroundColor Gray
& $javaExe -version 2>&1 | ForEach-Object { Write-Host $_ -ForegroundColor Gray }
Write-Host ""

# Run main build
$startTime = Get-Date
Write-Host "Running: .\mvnw.cmd clean verify -P slf4j-2.0,javadoc-validation --batch-mode" -ForegroundColor Cyan
& .\mvnw.cmd clean verify -P slf4j-2.0,javadoc-validation --batch-mode
$duration = (Get-Date) - $startTime

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[SUCCESS] Main build passed (JDK $mainBuildJdk, SLF4J 2.0)" -ForegroundColor Green
    Write-Host "Duration: $($duration.ToString('mm\:ss'))" -ForegroundColor Gray
    $results += [PSCustomObject]@{
        Job = "Build"
        JDK = $mainBuildJdk
        SLF4J = "slf4j-2.0"
        Status = "SUCCESS"
        Duration = $duration.ToString('mm\:ss')
    }
    $successCount++
    $totalBuilds++
} else {
    Write-Host "`n[FAILED] Main build failed (JDK $mainBuildJdk, SLF4J 2.0)" -ForegroundColor Red
    Write-Host "Duration: $($duration.ToString('mm\:ss'))" -ForegroundColor Gray
    $results += [PSCustomObject]@{
        Job = "Build"
        JDK = $mainBuildJdk
        SLF4J = "slf4j-2.0"
        Status = "FAILED"
        Duration = $duration.ToString('mm\:ss')
    }
    $failureCount++
    $totalBuilds++
    Write-Host "`nAborting matrix tests due to main build failure" -ForegroundColor Red
    exit 1
}

# ============================================
# JOB 2: Compatibility Tests Matrix
# ============================================
Write-Host "`n========================================" -ForegroundColor Magenta
Write-Host "JOB 2: Compatibility Tests Matrix" -ForegroundColor Magenta
Write-Host "========================================" -ForegroundColor Magenta
Write-Host "Testing: 7 combinations (compiled artifacts reused)`n" -ForegroundColor Gray

$testNumber = 0
foreach ($test in $testMatrix) {
    $testNumber++
    $jdkVersion = $test.JDK
    $slf4jProfile = $test.SLF4J
    $jdkPath = $jdks[$jdkVersion]

    Write-Host "`n[$testNumber/7] " -NoNewline -ForegroundColor Yellow
    Write-Host "Testing: JDK $jdkVersion + $slf4jProfile" -ForegroundColor Yellow
    Write-Host "----------------------------------------" -ForegroundColor Yellow

    # Verify JDK exists
    if (-not (Test-Path $jdkPath)) {
        Write-Host "ERROR: JDK $jdkVersion not found at $jdkPath" -ForegroundColor Red
        $results += [PSCustomObject]@{
            Job = "Test"
            JDK = $jdkVersion
            SLF4J = $slf4jProfile
            Profiles = $slf4jProfile
            Status = "FAILED"
            Duration = "0:00"
        }
        continue
    }

    # Set JAVA_HOME
    $env:JAVA_HOME = $jdkPath

    # Display Java version (compact)
    $javaExe = Join-Path $jdkPath "bin\java.exe"
    $javaVersionOutput = & $javaExe -version 2>&1 | Select-Object -First 1
    Write-Host "Java: $javaVersionOutput" -ForegroundColor Gray

    # Run tests (reusing compiled artifacts, just running tests)
    $startTime = Get-Date
    Write-Host "Running: .\mvnw.cmd test -P $slf4jProfile --batch-mode -Denforcer.skip=true" -ForegroundColor Cyan
    & .\mvnw.cmd test -P $slf4jProfile --batch-mode "-Denforcer.skip=true"
    $duration = (Get-Date) - $startTime

    if ($LASTEXITCODE -eq 0) {
        Write-Host "SUCCESS (JDK $jdkVersion, $slf4jProfile)" -ForegroundColor Green
        $results += [PSCustomObject]@{
            Job = "Test"
            JDK = $jdkVersion
            SLF4J = $slf4jProfile
            Status = "SUCCESS"
            Duration = $duration.ToString('mm\:ss')
        }
        $successCount++
    } else {
        Write-Host "FAILED (JDK $jdkVersion, $slf4jProfile)" -ForegroundColor Red
        $results += [PSCustomObject]@{
            Job = "Test"
            JDK = $jdkVersion
            SLF4J = $slf4jProfile
            Status = "FAILED"
            Duration = $duration.ToString('mm\:ss')
        }
        $failureCount++
    }
    $totalBuilds++
}

# ============================================
# Summary Report
# ============================================
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Build Matrix Summary" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Display results table
$results | Format-Table -Property @(
    @{Label="Job"; Expression={$_.Job}; Width=6},
    @{Label="JDK"; Expression={$_.JDK}; Width=4},
    @{Label="SLF4J"; Expression={$_.SLF4J}; Width=11},
    @{Label="Status"; Expression={$_.Status}; Width=8}
) -AutoSize

Write-Host "`nTotal: $totalBuilds builds/tests" -ForegroundColor Cyan
Write-Host "Success: $successCount" -ForegroundColor Green
Write-Host "Failures: $failureCount" -ForegroundColor Red

# Detailed failure report (if any)
if ($failureCount -gt 0) {
    Write-Host "`n========================================" -ForegroundColor Red
    Write-Host "Failed Builds/Tests" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    $results | Where-Object { $_.Status -eq "FAILED" } | ForEach-Object {
        Write-Host "[FAILED] $($_.Job): JDK $($_.JDK) + $($_.SLF4J)" -ForegroundColor Red
    }
}

# Exit with appropriate code
if ($failureCount -gt 0) {
    Write-Host "`nBuild matrix completed with failures" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`nAll builds and tests passed!" -ForegroundColor Green
    exit 0
}
