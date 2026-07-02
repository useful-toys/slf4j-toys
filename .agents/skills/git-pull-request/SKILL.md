---
name: git-pull-request
description: 'Writing conventions for Pull Request titles and descriptions in slf4j-toys — content only, not the git/gh mechanics of opening or merging one. Use whenever drafting, reviewing, or updating PR title/description text: the mandatory body sections (Context, Problem, Solution, Code Changes, Test Results), goal-oriented title format (not Conventional Commits), issue-closing keywords, and AI attribution phrasing. For the operational side (when to open a PR, how to create/merge it, branch lifecycle), see `trunk-based-development` instead.'
---

# Pull Request Writing Guidelines for slf4j-toys

This skill covers only the *content* of a PR — title and description text — not how to create, push, or merge one (that's `trunk-based-development`). A consistent PR structure lets reviewers who don't know this part of the codebase get oriented, see the problem, and judge the fix without re-deriving context from the diff alone. Follow the structure below for every PR description written in this repository.

## Language

Write the title, description, and any comments in English — the project's code, docs, and issue history are all English, and mixing languages fragments searchability and review.

## Title: goal-oriented, not Conventional Commits

PR titles describe the outcome for a human reviewer scanning a PR list, not a machine-parsed changelog entry — that's what commit messages are for (see `git-commit`). Keep them descriptive and specific enough to be understood without opening the PR.

**Good**: `Implement idempotent termination behavior for Meter` · `Add support for custom time sources in Watcher` · `Fix memory leak in Reporter context cleanup`

**Bad**: `feat(meter): implement idempotent termination` (Conventional Commits belongs in commits, not titles) · `Fix bug` / `Update tests` / `Changes` (not specific enough to scan)

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

- **`## Context`** — one or two sentences orienting a reviewer unfamiliar with this component, before showing them the problem.
- **`## Solution`** — the approach and key decisions, not a line-by-line diff walkthrough. Bold class/method names so they're scannable; name the pattern applied if there is one (guard clause, factory, etc.).
- **`## Code Changes`** — production vs. test files, with file and test counts, so a reviewer can judge PR size before opening a single diff.
- **`## Test Results`** — numbers, not just "tests pass" (e.g. "All 1710 Meter tests pass"). Run the relevant tier from `run-test` first; don't claim results you haven't observed.
- **`## Examples`** (recommended) — concrete before/after scenarios, faster for a reviewer to check against their mental model than re-reading the diff.
- **`## Relevant Documentation`** (optional) — links to TDRs, planning docs, or related PRs/issues that carry design context this PR relies on.

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

## AI attribution

Every AI-generated PR description must end with a co-author trailer naming the actual assistant and model that generated it — not a fixed placeholder tool name:

```markdown
---

Co-Authored-By: <Assistant Name> <Model> <contact address>
```

For Claude Code, that's the harness default: `Co-Authored-By: Claude Sonnet 5 <noreply@anthropic.com>`. If the PR is generated by GitHub Copilot specifically, follow `.github/copilot-instructions.md`'s override instead (`Co-authored-by: GitHub Copilot using <model name>`). Apply the same attribution convention consistently to commits authored in the same PR (see `git-commit`).

## Before finishing the draft

- All required sections above are present; `## API Changes` is included only if there's a public-facing change.
- `## Test Results` states numbers you actually observed (run the relevant tier from `run-test` first) — don't write it from assumption.
- AI attribution is present at the end of the description.

For turning this draft into an actual PR — when to open one, how to create and merge it — see `trunk-based-development`.

## Related Documentation

- `git-commit` skill — commit message conventions
- `trunk-based-development` skill — PR creation/merge mechanics and branch lifecycle
- [AGENTS.md](../../../AGENTS.md) — project AI agent guidelines
- [.github/copilot-instructions.md](../../../.github/copilot-instructions.md) — AI attribution standards
- [PR #44](https://github.com/useful-toys/slf4j-toys/pull/44) — complete example following this structure
