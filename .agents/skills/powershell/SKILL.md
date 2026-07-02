---
name: powershell
description: 'PowerShell conventions for working in this repository on Windows — JDK 21 setup, Maven wrapper invocation, argument quoting, and command syntax. Use whenever you are about to run a shell command in this repo (mvnw, git, java, or any CLI tool): before running Maven with -D/-P parameters containing #, @ or special characters; before chaining commands (no && or || here); before spawning any new shell, process, or background job; or whenever a command that works on Unix/Linux/macOS (ls, grep, tail, export, backslash line-continuation) needs a Windows-native equivalent.'
---

# PowerShell Usage in slf4j-toys

This repository is developed on Windows with PowerShell as the shell. Commands copied from Unix examples, other repos, or general knowledge often fail here in ways that look like flaky tooling but are actually syntax mismatches. Check the sections below before running an unfamiliar command.

## JDK 21 setup (once per terminal session)

Builds require JDK 21 on `PATH`/`JAVA_HOME` (see the `run-test` skill for why). Terminal sessions don't inherit it automatically, so set it explicitly at the start of each session:

```powershell
$jdk21 = Get-ChildItem -Path "$env:USERPROFILE\.jdks" -Filter "*21*" | Sort-Object -Property Name -Descending | Select-Object -First 1
if ($null -ne $jdk21) {
    $env:JAVA_HOME = $jdk21.FullName
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}
```

Verify it took effect before relying on it:

```powershell
java -version
$env:JAVA_HOME
```

## Stay in the current session

Reuse the existing terminal session for every command; don't spawn a new shell, sub-process, or background job to run one. A new process starts without the `JAVA_HOME`/`PATH` set up above and without the current working directory, so commands that worked a moment ago mysteriously fail or pick up the wrong Java version.

```powershell
# Correct — run directly in the current session
.\mvnw test
Get-ChildItem

# Wrong — each of these creates a new process that loses JAVA_HOME/PATH/cwd
powershell -Command ".\mvnw test"
cmd /c "mvnw test"
Start-Process powershell -ArgumentList "..."
bash -c "mvnw test"
Start-Job { .\mvnw test }
.\mvnw test &
```

## Quote Maven parameters containing special characters

PowerShell treats `#` as a comment start and interprets `@` and `=` specially. Wrap any `-D`/`-P` value containing these in single quotes so PowerShell passes it through literally instead of truncating it:

```powershell
# Correct — single quotes protect '#' from being parsed as a comment
.\mvnw test '-Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState'

# Wrong — PowerShell truncates at '#', so Maven only receives "MeterLifeCycleTest"
.\mvnw test -Dtest=MeterLifeCycleTest#shouldCreateMeterWithLoggerInitialState
```

Single quotes (`'...'`) are literal; double quotes (`"..."`) expand `$variables`. Prefer single quotes for Maven parameters even when a variable isn't involved, so a stray `$` in a test name can't be misinterpreted later.

## Use PowerShell-native commands, not Unix ones

Unix/bash commands are not available by default. Use their PowerShell equivalents:

| Unix | PowerShell |
| --- | --- |
| `ls` | `Get-ChildItem` |
| `tail -n 10 file` | `Get-Content -Tail 10 file` |
| `grep "pattern" file` | `Select-String "pattern" file` |
| `export VAR=value` | `$env:VAR = "value"` |

## Chain commands with `;`, not `&&`/`||`

`&&` and `||` are not valid PowerShell operators and raise a parse error. Use `;` to run commands in sequence:

```powershell
# Correct
.\mvnw compile; .\mvnw package

# Wrong — parse error: unexpected token '&&'
.\mvnw clean && .\mvnw test
```

## Other syntax differences

- **Line continuation**: use a trailing backtick (`` ` ``), not a backslash.
- **Path separators**: both `\` and `/` work; avoid mixing them in the same path for readability.
- **Environment variables**: read/write with `$env:NAME`, not bare `$NAME` or `export`.

## Related skills

- `run-test` — the Maven commands this skill's quoting rules apply to.
