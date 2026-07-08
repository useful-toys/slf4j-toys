---
name: create-issues
description: 'Create GitHub issues from AI-reported findings stored in the external findings directory (e.g., C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\). Use whenever converting analysis findings into trackable GitHub issues: reading finding files, extracting metadata into labels, creating issues via `gh issue create`, and closing resolved/accepted findings with resolution comments. Trigger on requests like "create issues", "convert findings to issues", "open GitHub issues", or any request to publish findings as repository issues — even when the word "issue" is not used explicitly.'
---

# Creating GitHub Issues from Findings

This skill converts findings produced by code reviews and audits (see `document-findings` and `code-review` skills) into trackable GitHub issues in the `useful-toys/slf4j-toys` repository, using a standardized English label taxonomy that mirrors the finding file metadata. All issue titles, bodies, comments, and labels must be written in English.

## When to use

- After an analysis run (e.g., `fable/01-watcher`) is complete and the user wants the findings tracked as GitHub issues.
- When the user asks to "create issues", "open issues", "convert findings to issues", or similar.
- When a revalidation sweep updates finding statuses and the corresponding GitHub issues need to be closed or updated.

## Prerequisites

- `gh` CLI authenticated with `repo` scope (`gh auth status`).
- Findings directory at `C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\` with `general.md` and per-finding `.md` files.
- Labels created in the repository (see Label Taxonomy below). If labels do not exist yet, create them first.

## Label Taxonomy

The label scheme uses `<dimension>:<value>` format with colons, grouped by dimension. Each dimension maps directly to a field in the finding file metadata table.

### Dimensions and Labels

| Dimension | Labels | Source in finding | Color |
|-----------|--------|-------------------|-------|
| **analyzer** | `analyzer:fable`, `analyzer:kimi`, `analyzer:sonet`, `analyzer:glm`, `analyzer:qodana`, `analyzer:codeql` | Directory name under `slf4j-toys-findings/` | `#666666` (gray) |
| **type** | `type:bug`, `type:sec`, `type:design`, `type:style`, `type:robust`, `type:doc`, `type:perf` | Filename prefix (`bug-`, `sec-`, `design-`, etc.) | `#0366d6` (blue) |
| **severity** | `severity:high`, `severity:medium`, `severity:low` | Filename severity segment or metadata table | `#d73a4a` / `#fbca04` / `#96f7d6` |
| **effort** | `effort:trivial`, `effort:small`, `effort:medium`, `effort:n/a` | Metadata table "Effort to fix" | `#c2e0c6` |
| **verdict** | `verdict:confirmed`, `verdict:plausible` | Metadata table "Verdict" | `#5319e7` (purple) |
| **status** | `status:pending`, `status:resolved`, `status:accepted` | Metadata table "Status" + filename suffix (`-fixed`, `-accepted`) | `#ffd54f` / `#28a745` / `#6f42c1` |
| **component** | `component:watcher`, `component:meter`, `component:reporter`, `component:logger`, `component:utils`, `component:config`, `component:build`, `component:docs`, `component:test` | Inferred from the "Where" field (package/module affected) | `#1d76db` |

### Creating labels

If labels do not exist in the repository, create them before creating issues:

```powershell
gh label create "analyzer:fable" --color "666666" --description "Finding produced by Fable analyzer"
gh label create "type:bug" --color "0366d6" --description "Bug - functional defect"
gh label create "severity:medium" --color "fbca04" --description "Medium severity"
gh label create "effort:small" --color "c2e0c6" --description "Small effort to fix"
gh label create "status:pending" --color "ffd54f" --description "Finding pending - not yet addressed"
# ... etc for all labels in the taxonomy
```

Run `gh label list --limit 100` first to check which labels already exist.

## Mapping findings to issues

### From filename

The finding filename encodes type, severity, and status:

```
<prefix>-<number>-<severity>-<slug>[-accepted][-fixed].md
```

- `prefix` → `type:<prefix>` label
- `severity` (`high`, `medium`, `low`) → `severity:<severity>` label
- `-fixed` suffix → `status:resolved` label (issue will be closed)
- `-accepted` suffix → `status:accepted` label (issue will be closed)
- no suffix → `status:pending` label (issue stays open)
- `-accepted-fixed` suffix → `status:resolved` label (issue will be closed; accepted risk with mitigation applied)

### From metadata table

The finding's metadata table (the `| | |` block) provides:

| Finding field | Label(s) |
|---------------|----------|
| Status row | `status:pending` / `status:resolved` / `status:accepted` |
| Verdict row | `verdict:confirmed` / `verdict:plausible` (extract the uppercase word) |
| Effort to fix row | `effort:trivial` / `effort:small` / `effort:medium` / `effort:n/a` |
| Severity row | `severity:high` / `severity:medium` / `severity:low` |

### From directory

The parent directory of the finding file is the analyzer name:

```
C:\git\slf4j-toys-findings\fable\01-watcher\robust-001-...md
                                ^^^^
                                analyzer:fable
```

### Component inference

Infer the `component:*` label from the "Where" field in the metadata table:

| Package/path in "Where" | Component label |
|------------------------|-----------------|
| `org.usefultoys.slf4j.watcher` | `component:watcher` |
| `org.usefultoys.slf4j.meter` | `component:meter` |
| `org.usefultoys.slf4j.reporter` (or `Report*`) | `component:reporter` |
| `org.usefultoys.slf4j` (LoggerFactory, streams, Session) | `component:logger` |
| `org.usefultoys.slf4j.utils` | `component:utils` |
| `WatcherConfig`, `MeterConfig`, `SystemConfig`, `SessionConfig` | `component:config` |
| `pom.xml`, `.github/workflows/`, `mvnw` | `component:build` |
| `README.md`, `doc/`, `wiki/` | `component:docs` |
| `src/test/` (test infrastructure only) | `component:test` |

If a finding spans multiple components, choose the primary one (the one where the fix would be applied).

## Issue body format

Each issue body should include (in English):

```markdown
## Summary

<one-paragraph summary from the finding's opening or "Problem" section>

## Where

- `<file:lines>` — branch: `<branch>`
- (list all locations from the "Where" field)

## Category

<from metadata table>

## Verdict

<from metadata table>

## Severity / Probability

<from metadata table>

## Effort

<from metadata table>

## Problem

<from the finding's "Problem" section — the mechanism description>

## Failure scenario

<from the finding's "Failure scenario" section>

## Suggested fix

<from the finding's "Suggested fix" or "Fix options" + "Recommended fix">

## Resolution

(Only for resolved/accepted findings — from the validation blockquote at the top of the file)

**FIXED** or **ACCEPTED** — validated on <date>, commit `<hash>`. <what the fix does>. <test evidence>.

## Traceability

- Commit(s): `<hash>`
- TDR(s): TDR-XXXX
- Analysis file: `<analyzer>/<NN-scope>/<finding-file>`
```

## Workflow

### 1. Read the findings

Read `general.md` for the overview and index, then read each finding `.md` file to extract:
- Title (from the `# ` heading)
- Metadata table fields
- Problem description
- Failure scenario
- Suggested fix
- Validation status (from blockquotes under the title)
- Resolution commit (from the validation blockquote or Status row)

### 2. Ensure labels exist

```powershell
gh label list --limit 100
```

Create any missing labels using `gh label create`.

### 3. Create issue body files

Write each issue body to a temporary file (e.g., `$env:LOCALAPPDATA\Temp\opencode\issue-<id>.md`). This avoids PowerShell quoting issues with `--body` on Windows.

### 4. Create issues

Use `gh issue create` with `--body-file` (or `-F`) and multiple `-l` flags:

```powershell
gh issue create `
    -t "DOC-001: WatcherConfig Javadoc outdated after singleton removal" `
    -F "$env:LOCALAPPDATA\Temp\opencode\issue-doc-001.md" `
    -l 'analyzer:fable' -l 'type:doc' -l 'verdict:confirmed' `
    -l 'severity:medium' -l 'effort:small' -l 'status:pending' -l 'component:watcher'
```

Capture the returned URL/number for each issue.

### 5. Close resolved and accepted issues

For findings with `status:resolved` or `status:accepted`, close the issue immediately after creation with a resolution comment:

```powershell
gh issue close <number> -c "Resolved in commit <hash>. <description>. <N/N tests pass>."
```

For accepted findings, the comment should reference the TDR or documentation that records the decision:

```powershell
gh issue close <number> -c "Accepted as project decision (TDR-XXXX). <what was documented>."
```

### 6. Verify

```powershell
gh issue list --state all --limit 30 --json number,title,labels,state
```

## PowerShell quoting notes

- Use `--body-file` (`-F`) instead of `--body` (`-b`) for multi-line bodies — PowerShell mangles strings with backticks, quotes, and special characters when passed inline.
- Wrap each `-l` value in single quotes: `-l 'severity:medium'`. The colon in label names is safe inside single quotes.
- Issue titles with special characters (accents, em-dashes) may need to be simplified for `gh` CLI compatibility on PowerShell. Replace accented characters with their ASCII equivalents in titles if `gh` rejects them.
- Comments passed via `-c` must also avoid characters that PowerShell interprets as argument separators. Keep comments concise and ASCII-safe.

## Relationship with other skills

- **`document-findings`**: Produces the finding files that this skill converts to issues. The finding files are the source of truth; GitHub issues are the tracking layer.
- **`code-review`**: Produces the analysis that `document-findings` persists and this skill publishes.
- **`trunk-based-development`**: Creating labels and issues is a repository configuration action (section 7 exception) — it can be done directly on `main` without a branch/PR. However, if the user asks for the skill file itself (under `.agents/skills/`) to be created or updated, that is also a section 7 exception.
- **`git-commit-message`**: If committing the skill file or any repo changes, follow the conventional commit format.

## Related files

- Findings directory: `C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\`
- Skills directory: `.agents/skills/`
- `document-findings` skill: `.agents/skills/document-findings/SKILL.md`
- `code-review` skill: `.agents/skills/code-review/SKILL.md`

## Language

All issue titles, bodies, comments, and labels must be in English. Translate any Portuguese finding fields (e.g., `Resumo` → `Summary`, `Problema` → `Problem`, `Cenário de falha` → `Failure scenario`) when creating the GitHub issue.
