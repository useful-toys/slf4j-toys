---
name: git-pull-request
description: 'Structure and conventions for Pull Request titles and descriptions in slf4j-toys. Use whenever opening a PR (gh pr create), drafting or updating a PR description, or writing PR/issue-closing text — covers the mandatory body sections (Context, Problem, Solution, Code Changes, Test Results), goal-oriented title format (not Conventional Commits), issue-closing keywords, and AI attribution.'
---

# Pull Request Guidelines for slf4j-toys

A consistent PR structure lets reviewers who don't know this part of the codebase get oriented, see the problem, and judge the fix without re-deriving context from the diff alone. Follow the structure below for every PR opened in this repository.

## Language

Write the title, description, and any comments in English — the project's code, docs, and issue history are all English, and mixing languages fragments searchability and review.

## Title: goal-oriented, not Conventional Commits

PR titles describe the outcome for a human reviewer scanning a PR list, not a machine-parsed changelog entry — that's what commit messages are for (see `git-commit-push`). Keep them descriptive and specific enough to be understood without opening the PR.

**Good**: `Implement idempotent termination behavior for Meter` · `Add support for custom time sources in Watcher` · `Fix memory leak in Reporter context cleanup`

**Bad**: `feat(meter): implement idempotent termination` (Conventional Commits belongs in commits, not titles) · `Fix bug` / `Update tests` / `Changes` (not specific enough to scan)

> If this repo squash-merges PRs with the PR title as the resulting commit subject, reconcile this rule with `trunk-based-development`'s requirement that the squash-commit subject follow Conventional Commits — check which one is authoritative before assuming both hold simultaneously.

## Closing issues

If the PR resolves one or more issues, reference them at the top of the description using GitHub's closing keywords so the issue closes automatically on merge and reviewers get the linked context:

```markdown
Fixes #123
Resolves #456, #457
Closes useful-toys/slf4j-toys#789
```

Use `Fixes` for bug fixes, `Resolves` for features/improvements, `Closes` when neither fits better.

## Required body sections

Write the sections below in order. Each one answers a question a reviewer will otherwise have to dig for in the diff.

| Section | Required? | Answers |
|---|---|---|
| Issue references | If applicable | Which issue(s) does this close? |
| `## Context` | Yes | What area of the code is this touching, and why does it exist? |
| `## Problem` | Yes | What's wrong or missing today? |
| `## Solution` | Yes | How was it fixed, and what design choices were made? |
| `## API Changes` | If applicable | What public signatures/behavior changed, and is it backward compatible? |
| `## Code Changes` | Yes | Which files, how many tests, at a glance? |
| `## Test Results` | Yes | Proof the change works and doesn't regress anything |
| `## Examples` | Recommended | Concrete before/after usage |
| `## Relevant Documentation` | If applicable | Links to TDRs, plans, related PRs |

### `## Context`

Orient a reviewer unfamiliar with this component before showing them the problem.

```markdown
## Context

The Meter class is a core component of slf4j-toys that tracks operation
lifecycle (start, progress, termination). Currently, termination methods
(ok, reject, fail) can be called multiple times on an already stopped
meter, overwriting the state from the first termination.
```

### `## Problem`

Show the incorrect behavior concretely — a short code snippet beats a prose description. Mark the wrong outcome with ❌ and state the impact (correctness, data loss, surprising API behavior, etc.).

```markdown
## Problem

When a meter is already stopped, subsequent termination calls incorrectly
overwrite the path and outcome from the first termination:

\`\`\`java
meter.start();
meter.ok();              // Stops meter with okPath=null
meter.reject("error");   // OVERWRITES to rejectPath="error" ❌
// Expected: reject should be blocked, preserving okPath
\`\`\`

This violates the principle that the first termination should win.
```

### `## Solution`

Explain the approach and the key decisions — not a line-by-line diff walkthrough. Bold class/method names so they're scannable; name the pattern applied if there is one (guard clause, factory, etc.).

```markdown
## Solution

Implemented idempotent termination behavior using guard clauses:

1. **MeterValidator.validateStopPrecondition()** now returns `boolean`:
   - Returns `false` when meter already stopped (blocks re-termination)
   - Returns `true` otherwise (allows termination with warnings)

2. **Meter termination methods** check the validation result:
   - commonOk(), reject(), fail() call validateStopPrecondition()
   - Early return when meter already stopped
   - Preserves path and state from first termination
```

### `## API Changes` — omit if there are none

Flag anything a caller could notice: signature changes, new behavior, new public API. State backward compatibility explicitly rather than leaving reviewers to infer it.

```markdown
## API Changes

### Modified Signature

**MeterValidator.validateStopPrecondition()**:
\`\`\`java
// Before:
public static void validateStopPrecondition(final Meter meter, final Marker marker)

// After:
public static boolean validateStopPrecondition(final Meter meter, final Marker marker)
\`\`\`

### Modified Behavior

**Meter.ok() / ok(pathId) / reject(cause) / fail(cause)**:
- Now check if meter already stopped before executing
- Return immediately when already stopped
- Preserve path and state from first termination

**Backward Compatibility**: Backward compatible — no client-facing change, only internal behavior.
```

### `## Code Changes`

Give the scope at a glance: production vs. test files, with counts, so a reviewer can judge PR size before opening a single diff.

```markdown
## Code Changes

### Production Code (2 files)
- **MeterValidator.java**: Changed validateStopPrecondition() return type
- **Meter.java**: Added guard clauses in commonOk(), reject(), fail()

### Test Code (3 files, 86 tests updated)
- **MeterValidatorTest.java** (4 tests): Capture and assert boolean returns
- **MeterLogBugTest.java** (6 tests): Fixed mocks for exception handling
- **MeterLifeCyclePreStartTerminatedPostStopInvalidTerminationTest.java** (28 tests):
  - Removed "Will be fixed in future" comments
  - Fixed state assertions and event counts
```

### `## Test Results`

State that the change actually works, with numbers, not just "tests pass." Run the relevant tier from `run-test` before writing this section — don't claim results you haven't observed.

```markdown
## Test Results

✅ **All 1710 Meter tests pass**

Comprehensive validation confirms:
- Idempotent termination works correctly
- First termination always wins in all scenarios
- No regression in existing functionality
- Backward compatibility maintained
```

### `## Examples` (recommended)

Concrete before/after scenarios help a reviewer confirm the fix matches their mental model of the bug, faster than re-reading the diff.

```markdown
## Examples

### Scenario 1: Double termination after start
\`\`\`java
final Meter meter = new Meter(logger).start();
meter.ok();              // Terminates with okPath=null
meter.reject("error");   // REJECTED: okPath preserved ✅
\`\`\`

### Scenario 2: Termination with path
\`\`\`java
final Meter meter = new Meter(logger).start();
meter.ok("SUCCESS");     // Terminates with okPath="SUCCESS"
meter.ok("ALTERNATE");   // REJECTED: okPath remains "SUCCESS" ✅
\`\`\`
```

### `## Relevant Documentation` (optional)

Link TDRs, planning docs, or related PRs/issues that carry design context this PR relies on.

## AI attribution

`.github/copilot-instructions.md` overrides AI attribution project-wide for GitHub Copilot: every AI-generated PR description must end with

```markdown
---

Co-authored-by: GitHub Copilot using <model name>
```

where `<model name>` is the actual model used (e.g., `Claude Sonnet 4.5`). Apply the same attribution convention consistently to commits authored in the same PR (see `git-commit-push`).

## Creating the PR with GitHub CLI

Prefer a body file over an inline `-body` string — PowerShell mangles multi-line strings with embedded code blocks and special characters, a body file doesn't:

```powershell
$prBody = @'
Resolves #123

## Context

Your PR description here...

---

Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
'@

$prBody | Out-File -FilePath pr-body.txt -Encoding UTF8
gh pr create --title "Your descriptive title" --body-file pr-body.txt
Remove-Item pr-body.txt
```

For a quick PR without scripting the body, `gh pr create --editor` opens the default editor for title and body instead.

After creating it, open the PR URL `gh pr create` prints and confirm the sections render as intended — code blocks and issue-closing keywords are the two things most likely to look right in the terminal but wrong on GitHub.

## Before opening the PR

- Tests relevant to the change pass locally (see `run-test`) — don't write the `## Test Results` section from assumption.
- All required sections above are present; `## API Changes` is included only if there's a public-facing change.
- The PR contains one concern — split unrelated changes into separate PRs (see `trunk-based-development` for branch-size guidance).
- AI attribution is present at the end of the description.

## Related Documentation

- `git-commit-push` skill — commit message conventions
- `trunk-based-development` skill — when a PR is opened relative to branch/merge lifecycle
- [AGENTS.md](../../../AGENTS.md) — project AI agent guidelines
- [.github/copilot-instructions.md](../../../.github/copilot-instructions.md) — AI attribution standards
- [PR #44](https://github.com/useful-toys/slf4j-toys/pull/44) — complete example following this structure
