#requires -Version 7
<#
.SYNOPSIS
    Stamps a JMH JSON result file with the Git provenance of the slf4j-toys
    library it measured, so a benchmark baseline can be traced back to the exact
    commit it was run against.

.DESCRIPTION
    JMH's JSON output is a plain top-level array of per-benchmark result objects,
    with no slot for run-level metadata. This script wraps that array into

        {
          "provenance": { ...git.* ..., "available": true, ... },
          "results":    [ <the original JMH array, unchanged> ]
        }

    The provenance is read from META-INF/git.properties inside the *measured*
    library jar (the same SNAPSHOT the benchmark module resolved from the local
    Maven repository), falling back to the jar manifest's SCM-* entries. Because
    the benchmarks always measure the installed library, that jar's commit - not
    this benchmark module's own checkout - is the meaningful provenance.

    Typical use, right after JMH has written its result file:

        .\mvnw -DskipTests install
        .\mvnw -f benchmarks\pom.xml compile exec:java "-Dexec.args=... -rf json -rff after.json"
        .\benchmarks\stamp-provenance.ps1 after.json

    The results array is embedded verbatim (as text), so numbers and formatting
    are preserved byte-for-byte. The operation is idempotent: re-stamping an
    already-wrapped file refreshes the provenance block and keeps the results.

.PARAMETER JsonPath
    Path to the JMH result JSON to stamp. Rewritten in place unless -OutPath is
    given.

.PARAMETER JarPath
    Explicit path to the measured library jar. When omitted, the jar is located
    in the local Maven repository from the slf4j-toys dependency version declared
    in the benchmarks POM.

.PARAMETER PomPath
    Path to the benchmarks POM used to resolve the library version. Defaults to
    pom.xml next to this script.

.PARAMETER OutPath
    Write the stamped JSON here instead of overwriting JsonPath.

.PARAMETER AllowMissing
    Stamp with provenance.available = false instead of failing when no provenance
    is found in the jar (e.g. a library built before the provenance feature, or
    built on a machine without git).

.EXAMPLE
    .\benchmarks\stamp-provenance.ps1 after.json

.EXAMPLE
    .\benchmarks\stamp-provenance.ps1 -JsonPath raw.json -OutPath after-stamped.json
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory, Position = 0)][string]$JsonPath,
    [string]$JarPath,
    [string]$PomPath,
    [string]$OutPath,
    [switch]$AllowMissing
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Resolve-LibraryJar {
    param([string]$PomPath)

    [xml]$pom = Get-Content -Raw -LiteralPath $PomPath
    $ns = @{ m = 'http://maven.apache.org/POM/4.0.0' }
    $dep = Select-Xml -Xml $pom -Namespace $ns `
        -XPath "//m:dependencies/m:dependency[m:groupId='org.usefultoys' and m:artifactId='slf4j-toys']" |
        Select-Object -First 1
    if (-not $dep) {
        throw "Could not find the org.usefultoys:slf4j-toys dependency in $PomPath"
    }
    $version = $dep.Node.version
    $repo = if ($env:MAVEN_REPO) { $env:MAVEN_REPO } else { Join-Path $HOME '.m2/repository' }
    $jar = Join-Path $repo "org/usefultoys/slf4j-toys/$version/slf4j-toys-$version.jar"
    if (-not (Test-Path -LiteralPath $jar)) {
        throw "Measured library jar not found: $jar`n" +
              "Install it first from the library you want to measure: .\mvnw -DskipTests install"
    }
    return $jar
}

function Read-JarEntry {
    param([string]$JarPath, [string]$EntryName)

    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($JarPath)
    try {
        $entry = $zip.GetEntry($EntryName)
        if (-not $entry) { return $null }
        $reader = New-Object System.IO.StreamReader($entry.Open())
        try { return $reader.ReadToEnd() } finally { $reader.Dispose() }
    } finally {
        $zip.Dispose()
    }
}

# Reads git.properties (preferred) or, failing that, the SCM-* manifest entries
# from the measured jar. Returns an ordered map of raw string key/value pairs and
# a label describing which source was used.
function Get-Provenance {
    param([string]$JarPath)

    $prov = [ordered]@{}
    $source = $null

    $gitProps = Read-JarEntry -JarPath $JarPath -EntryName 'META-INF/git.properties'
    if ($gitProps) {
        foreach ($line in ($gitProps -split "`r?`n")) {
            if ($line -match '^\s*#' -or $line -notmatch '=') { continue }
            $parts = $line -split '=', 2
            $k = $parts[0].Trim()
            $v = $parts[1].Trim()
            if ($k) { $prov[$k] = $v }
        }
        if ($prov.Count -gt 0) { $source = 'META-INF/git.properties' }
    }

    if ($prov.Count -eq 0) {
        $mf = Read-JarEntry -JarPath $JarPath -EntryName 'META-INF/MANIFEST.MF'
        if ($mf) {
            foreach ($line in ($mf -split "`r?`n")) {
                if ($line -match '^(SCM-[^:]+):\s*(.*)$') {
                    $prov[$Matches[1]] = $Matches[2].Trim()
                }
            }
            if ($prov.Count -gt 0) { $source = 'META-INF/MANIFEST.MF (SCM-*)' }
        }
    }

    return @{ Values = $prov; Source = $source }
}

# ---- resolve inputs -------------------------------------------------------

if (-not (Test-Path -LiteralPath $JsonPath)) {
    throw "JMH result file not found: $JsonPath"
}
if (-not $PomPath) {
    $PomPath = Join-Path $PSScriptRoot 'pom.xml'
}
if (-not $JarPath) {
    $JarPath = Resolve-LibraryJar -PomPath $PomPath
}
if (-not (Test-Path -LiteralPath $JarPath)) {
    throw "Measured library jar not found: $JarPath"
}

# ---- read provenance from the measured jar --------------------------------

$read = Get-Provenance -JarPath $JarPath
$provRaw = $read.Values
if ($provRaw.Count -eq 0 -and -not $AllowMissing) {
    throw "No provenance found in $JarPath (neither META-INF/git.properties nor SCM-* " +
          "manifest entries).`nRebuild/reinstall the library at a commit that carries " +
          "provenance (.\mvnw -DskipTests install), or pass -AllowMissing to stamp anyway."
}

$provenance = [ordered]@{}
$provenance['available']    = ($provRaw.Count -gt 0)
$provenance['source']       = "$(Split-Path -Leaf $JarPath)$(if ($read.Source) { " ($($read.Source))" })"
$provenance['stampedAtUtc'] = (Get-Date).ToUniversalTime().ToString('o')
foreach ($k in $provRaw.Keys) {
    $v = $provRaw[$k]
    if ($k -eq 'git.dirty' -and ($v -eq 'true' -or $v -eq 'false')) {
        $provenance[$k] = ($v -eq 'true')
    } else {
        $provenance[$k] = $v
    }
}

# ---- extract the JMH results array as text --------------------------------

$raw = (Get-Content -Raw -LiteralPath $JsonPath).Trim()
if ($raw.StartsWith('[')) {
    # Plain JMH output: embed the array verbatim to preserve every value exactly.
    $resultsText = $raw
} elseif ($raw.StartsWith('{')) {
    # Already wrapped by a previous run: re-stamp, keeping the results.
    $obj = $raw | ConvertFrom-Json -Depth 200
    if (-not ($obj.PSObject.Properties.Name -contains 'results')) {
        throw "$JsonPath is a JSON object without a 'results' array; refusing to overwrite it."
    }
    $resultsText = $obj.results | ConvertTo-Json -Depth 200
} else {
    throw "$JsonPath does not look like JMH JSON output (expected a leading '[' or '{')."
}

# ---- compose the wrapped document -----------------------------------------

$provJson = ($provenance | ConvertTo-Json -Depth 5)
$provIndented    = ($provJson    -split "`r?`n") -join "`n  "
$resultsIndented = ($resultsText -split "`r?`n") -join "`n  "

$out = "{`n  ""provenance"": $provIndented,`n  ""results"": $resultsIndented`n}`n"

$target = if ($OutPath) { $OutPath } else { $JsonPath }
Set-Content -LiteralPath $target -Value $out -Encoding utf8 -NoNewline

# Status summary goes to the information stream (not stdout), so it stays visible
# without polluting any pipeline that captures the script's output.
Write-Information "Stamped $target with library provenance:" -InformationAction Continue
foreach ($entry in $provenance.GetEnumerator()) {
    Write-Information ("  {0,-22} {1}" -f $entry.Key, $entry.Value) -InformationAction Continue
}
if (-not $provenance['available']) {
    Write-Warning "Provenance was NOT available in the measured jar; recorded available=false."
}
