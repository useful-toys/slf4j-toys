---
name: create-issues
description: 'Create GitHub issues from AI-reported findings stored in the external findings directory (e.g., C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\). Use whenever converting analysis findings into trackable GitHub issues: reading finding files, extracting metadata into labels, creating issues via `gh issue create`, and closing resolved/accepted findings with resolution comments. Trigger on requests like "crie issues", "converta achados em issues", "abra issues no GitHub", or any request to publish findings as repository issues — even when the word "issue" is not used explicitly.'
---

# Creating GitHub Issues from Findings

This skill converts findings produced by code reviews and audits (see `document-findings` and `code-review` skills) into trackable GitHub issues in the `useful-toys/slf4j-toys` repository, using a standardized label taxonomy that mirrors the finding file metadata.

## When to use

- After an analysis run (e.g., `fable/01-watcher`) is complete and the user wants the findings tracked as GitHub issues.
- When the user asks to "create issues", "open issues", "convert findings to issues", or similar.
- When a revalidation sweep updates finding statuses and the corresponding GitHub issues need to be closed or updated.

## Prerequisites

- `gh` CLI authenticated with `repo` scope (`gh auth status`).
- Findings directory at `C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\` with `geral.md` and per-finding `.md` files.
- Labels created in the repository (see Label Taxonomy below). If labels do not exist yet, create them first.

## Label Taxonomy

The label scheme uses `<dimension>:<value>` format with colons, grouped by dimension. Each dimension maps directly to a field in the finding file metadata table.

### Dimensions and Labels

| Dimension | Labels | Source in finding | Color |
|-----------|--------|-------------------|-------|
| **analyzer** | `analyzer:fable`, `analyzer:kimi`, `analyzer:sonet`, `analyzer:glm`, `analyzer:qodana`, `analyzer:codeql` | Directory name under `slf4j-toys-findings/` | `#666666` (gray) |
| **type** | `type:bug`, `type:sec`, `type:design`, `type:style`, `type:robust`, `type:doc`, `type:perf` | Filename prefix (`bug-`, `sec-`, `design-`, etc.) | `#0366d6` (blue) |
| **severity** | `severity:alta`, `severity:media`, `severity:baixa` | Filename severity segment or metadata table | `#d73a4a` / `#fbca04` / `#96f7d6` |
| **effort** | `effort:trivial`, `effort:pequeno`, `effort:medio`, `effort:n/a` | Metadata table "Esforço para corrigir" | `#c2e0c6` |
| **verdict** | `verdict:confirmed`, `verdict:plausible` | Metadata table "Veredito" | `#5319e7` (purple) |
| **status** | `status:pendente`, `status:resolvido`, `status:aceito` | Metadata table "Status" + filename suffix (`-fixed`, `-accepted`) | `#ffd54f` / `#28a745` / `#6f42c1` |
| **component** | `component:watcher`, `component:meter`, `component:reporter`, `component:logger`, `component:utils`, `component:config`, `component:build`, `component:docs`, `component:test` | Inferred from the "Onde" field (package/module affected) | `#1d76db` |

### Creating labels

If labels do not exist in the repository, create them before creating issues:

```powershell
gh label create "analyzer:fable" --color "666666" --description "Finding produced by Fable analyzer"
gh label create "type:bug" --color "0366d6" --description "Bug - functional defect"
gh label create "severity:media" --color "fbca04" --description "Medium severity"
# ... etc for all labels in the taxonomy
```

Run `gh label list --limit 100` first to check which labels already exist.

## Mapping findings to issues

### From filename

The finding filename encodes type, severity, and status:

```
<prefixo>-<numero>-<severidade>-<slug>[-accepted][-fixed].md
```

- `prefixo` → `type:<prefixo>` label
- `severidade` (`alta`, `media`, `baixa`) → `severity:<severidade>` label
- `-fixed` suffix → `status:resolvido` label (issue will be closed)
- `-accepted` suffix → `status:aceito` label (issue will be closed)
- no suffix → `status:pendente` label (issue stays open)
- `-accepted-fixed` suffix → `status:resolvido` label (issue will be closed; accepted risk with mitigation applied)

### From metadata table

The finding's metadata table (the `| | |` block) provides:

| Finding field | Label(s) |
|---------------|----------|
| Status row | `status:pendente` / `status:resolvido` / `status:aceito` |
| Veredito row | `verdict:confirmed` / `verdict:plausible` (extract the uppercase word) |
| Esforço para corrigir row | `effort:trivial` / `effort:pequeno` / `effort:medio` / `effort:n/a` |
| Severidade row | `severity:alta` / `severity:media` / `severity:baixa` |

### From directory

The parent directory of the finding file is the analyzer name:

```
C:\git\slf4j-toys-findings\fable\01-watcher\robust-001-...md
                                ^^^^
                                analyzer:fable
```

### Component inference

Infer the `component:*` label from the "Onde" field in the metadata table:

| Package/path in "Onde" | Component label |
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

Each issue body should include:

```markdown
## Resumo

<one-paragraph summary from the finding's opening or "Problema" section>

## Onde

- `<file:lines>` — branch: `<branch>`
- (list all locations from the "Onde" field)

## Categoria

<from metadata table>

## Veredito

<from metadata table>

## Severidade / Probabilidade

<from metadata table>

## Esforço

<from metadata table>

## Problema

<from the finding's "Problema" section — the mechanism description>

## Cenário de falha

<from the finding's "Cenário de falha" section>

## Correção sugerida

<from the finding's "Correção sugerida" or "Opções de correção" + "Correção recomendada">

## Resolução

(Only for resolved/accepted findings — from the validation blockquote at the top of the file)

**CORRIGIDO** or **ACEITO** — validated on <date>, commit `<hash>`. <what the fix does>. <test evidence>.

## Rastreabilidade

- Commit(s): `<hash>`
- TDR(s): TDR-XXXX
- Analysis file: `<analyzer>/<NN-scope>/<finding-file>`
```

## Workflow

### 1. Read the findings

Read `geral.md` for the overview and index, then read each finding `.md` file to extract:
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
    -t "DOC-001: Javadoc de WatcherConfig desatualizado apos remocao do singleton" `
    -F "$env:LOCALAPPDATA\Temp\opencode\issue-doc-001.md" `
    -l 'analyzer:fable' -l 'type:doc' -l 'verdict:confirmed' `
    -l 'severity:media' -l 'effort:pequeno' -l 'status:pendente' -l 'component:watcher'
```

Capture the returned URL/number for each issue.

### 5. Close resolved and accepted issues

For findings with `status:resolvido` or `status:aceito`, close the issue immediately after creation with a resolution comment:

```powershell
gh issue close <number> -c "Resolvido em commit <hash>. <description>. <N/N testes passam>."
```

For accepted findings, the comment should reference the TDR or documentation that records the decision:

```powershell
gh issue close <number> -c "Aceito por decisao de projeto (TDR-XXXX). <what was documented>."
```

### 6. Verify

```powershell
gh issue list --state all --limit 30 --json number,title,labels,state
```

## PowerShell quoting notes

- Use `--body-file` (`-F`) instead of `--body` (`-b`) for multi-line bodies — PowerShell mangles strings with backticks, quotes, and special characters when passed inline.
- Wrap each `-l` value in single quotes: `-l 'severity:media'`. The colon in label names is safe inside single quotes.
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
