---
name: document-findings
description: 'Format and lifecycle for persisting analysis findings of slf4j-toys as Markdown files under an external findings directory (e.g., C:\git\slf4j-toys-findings\<analyzer>\<NN-scope>\). Use whenever saving new findings from a code review or audit, updating an existing finding (status change, fix validation, accepted-risk classification), renaming finding files with -accepted/-fixed suffixes, or maintaining the geral.md index. Trigger on requests like "guarde os achados", "documente as questões encontradas", "valide se o achado X foi corrigido", "marque como resolvido/aceito", or any request to record or revisit review results — even if the word "finding" is not used.'
---

# Documenting Analysis Findings

This skill defines the file format, naming, and lifecycle for findings produced by code reviews and audits of slf4j-toys. Findings are analysis deliverables for the user — they live **outside the repository** so they never mix with source code, survive branch switches, and can be compared across analyzers.

The analysis methodology itself (how to find issues) is the `code-review` skill. This skill covers only how to **persist and maintain** what was found.

## Directory layout

```
C:\git\slf4j-toys-findings\
    <analyzer>\              e.g. fable, glm, qodana, codeql
        <NN-scope>\          e.g. 01-watcher, 02-meter — NN keeps analyses ordered
            geral.md         overall assessment + index (see below)
            <finding files>
```

One directory per analysis run/scope. The `<analyzer>` is the tool or agent that produced the findings; the scope is usually a package or feature branch under review.

## Language

Finding files are user deliverables, not project documentation. Write them in **pt-BR** (the project's English-only rule applies to `doc/*.md`, `README.md`, Javadoc, and code — not to findings). Code snippets, commit hashes, and quoted Javadoc stay verbatim.

## File naming

```
<prefixo>-<numero>-<severidade>-<slug>[-accepted][-fixed].md
```

- **prefixo** — `bug`, `sec`, `design`, `style`, `robust`, `doc`, `perf` (same meanings as in the `code-review` skill). Number sequentially per prefix (`001`, `002`, …).
- **severidade** — `alta`, `media`, `baixa` (no accents in filenames).
- **slug** — short kebab-case description in pt-BR.
- **`-accepted` suffix** — append when a Javadoc, TDR, or code comment explicitly documents the issue as an assumed risk or design decision. Never infer acceptance; it must be written somewhere in the repo.
- **`-fixed` suffix** — append when the finding was resolved **and the fix was validated** (see lifecycle below). A finding can carry both (`-accepted-fixed`) when an accepted decision later gets its recommended mitigation applied.

Example: `robust-003-media-excecao-encerra-agendamento-silenciosamente-fixed.md`

## Per-finding file template

```markdown
# <PREFIXO>-<NNN>: <Título — uma frase que afirma o defeito>

> **STATUS: ...** (validation blockquotes, newest first — an append-only treatment
> history; see "Treatment history" below)

| | |
|---|---|
| **Onde** | `<path>:<linhas>` (+ métodos) — branch: `<branch onde foi achado>` |
| **Status** | Pendente / Resolvido (...) / Aceito (...) |
| **Categoria** | <dimensão: Robustez, Concorrência, Segurança, Design, ...> |
| **Veredito** | CONFIRMED / PLAUSIBLE — com justificativa curta |
| **Severidade / Probabilidade** | Alta|Média|Baixa / Sempre|Rara|... |
| **Esforço para corrigir** | Trivial / Pequeno / Médio / N/A (aceito) |
| **Introduzido em** | `<commit>` (<resumo do commit>) |
| **CWE** | CWE-NNN (somente para `sec`) |

**Código atual** (`<arquivo>:<linhas>`):

​```java
// exact problematic lines, enough context to locate them
​```

**Problema:** <mechanism — trace the code path that triggers the issue; reference concrete lines>

**Cenário de falha:** <concrete inputs/state → wrong outcome; what the user/operator experiences>

**Opções de correção:**

1. **<Opção A — nome curto>**: <concrete fix, with code when it helps; note Java 8
   compatibility when relevant>
   - *Vantagens:* <what it solves, simplicity, cost>
   - *Desvantagens:* <trade-offs, new risks, effort>
   - *Consequências:* <API/behavior changes, breaking changes, impact on tests/docs>
2. **<Opção B>**: ...

**Correção recomendada:** Opção <X> — <why it wins over the alternatives for THIS project:
alignment with existing patterns/TDRs, cost/benefit, risk profile>.

> **Nota**: <optional caveats — heritage from a previous analysis, why the suffix was applied, etc.>
```

Rules that make findings useful:

- **Onde must name the branch.** Findings outlive branches; without the branch name it becomes impossible to tell whether the analyzed code is `main`, a feature worktree, or something already merged. When a finding spans branches, say which part lives where (e.g., "branch: `main` (controllers); `feat/x` (servlets)").
- **Every claim points at code.** File, line numbers, commit hash. If you cannot point at the line, it is not a finding.
- **Cenário de falha is concrete.** Not "may cause issues" but "a JMX query throws once → monitoring stops forever, silently, while `isRunning()` returns true".
- **Offer options, then commit to one.** Present one or more fix options, each with
  advantages, disadvantages, and consequences — the maintainer decides under constraints the
  analyzer may not see (release timing, API stability, backlog). But do not hide behind a menu:
  always name the **recommended option** and justify it against the alternatives (consistency
  with existing patterns/TDRs, cost/benefit, residual risk). A single obvious fix is fine as a
  one-option list — the recommendation and its rationale are still required. When a fix is later
  applied, the validation blockquote should say which option (or which variation) was chosen.

## Status lifecycle

| Status | Meaning | Filename |
|--------|---------|----------|
| **Pendente** | Reported, not addressed | no suffix |
| **Aceito** | Risk assumed, documented in Javadoc/TDR | `-accepted` |
| **Resolvido** | Fixed **and validated** | `-fixed` |

### Marking a finding Resolvido

Never mark a finding resolved on someone's word alone — validate first:

1. Locate the fix: commit hash on which branch, or uncommitted changes in which worktree (`git status`/`git diff` there).
2. Read the diff and check it actually addresses the mechanism described in the finding (not just the symptom).
3. Run the relevant tests (`run-test` skill) and record the result (e.g., "24/24 testes passam").
4. Add a **status blockquote** right under the H1 title: date, commit, what the fix does, test evidence, residual limitations if any.
5. Update the **Status** row and the **Onde** row (add "corrigido em `<branch>` (`<commit>`)").
6. Rename the file adding `-fixed`.
7. Update `geral.md`: index link + annotation, statistics table, and any test-coverage gap the fix closed.

If the fix was found uncommitted, say so — and update the note once it lands in a commit.

### Marking melhorias on an Aceito finding

Accepted findings can still carry cheap recommendations. When those get applied, keep status **Aceito**, add a validation blockquote describing what improved, and annotate the Status row ("melhorias recomendadas aplicadas e validadas em <data>"). Add `-fixed` only if the user considers the finding's core risk addressed.

### Validation blockquote format

```markdown
> **STATUS: CORRIGIDO — validado em <data>, commitado como `<hash>` em `<branch>`.** <o que a
> correção faz>. <testes novos/ajustados>. Validação: `<comando de teste>` — <N/N testes passam>.
> <limitação residual, se houver>.
```

For a negative validation (checked, not fixed yet), record that too — with date and what exactly was checked — so the next session does not redo the work.

### Treatment history — validation entries are append-only

The blockquotes under the H1 title are the finding's **treatment history**: a chronological
record of every attempt to fix, mitigate, or improve the issue, and of every review of those
attempts. This history is why a finding file outlives its own resolution — it shows *how* the
problem was approached, which attempts failed or only partially worked, and why the final state
is what it is. A reviewer reading the file six months later should be able to reconstruct the
whole path without digging through git logs of a possibly deleted branch.

Therefore:

- **Never delete or rewrite a previous validation entry.** When reviewing an updated version of
  the code that supposedly adjusts/fixes/improves the issue, **append a new blockquote above the
  existing ones** (newest first), with its own date, commit, description of what the change
  attempts, and verdict. Earlier entries stay untouched — a superseded "NÃO corrigido" entry is
  not wrong, it is history; the newer entry's date shows it was superseded.
- Each entry describes the **attempt**, not just the verdict: what the change did (which option
  from "Opções de correção", or a different approach), and how it affected the finding —
  resolved, partially resolved (what remains), improved without resolving, or made no
  difference (why the claim does not hold).
- Only the **Status** table row and the filename reflect the *current* state; the blockquote
  stack preserves the trajectory. Small factual corrections to the latest entry (e.g., an
  "uncommitted" note once the fix lands in a commit) may update that entry in place — that is
  refining the same event, not erasing an attempt.

### Revalidating an entire analysis

When asked to verify whether the findings of an analysis were resolved — typically after
another agent or developer claims to have fixed "all" of them — do not sample: **sweep every
finding in the directory** and leave no file without a recorded outcome. The claim is the thing
under test, and partial verification cannot refute it.

1. Pin the state under review once (branch tip hash, worktree clean or not) and reuse it in
   every blockquote, so all verdicts are comparable.
2. Read the diff of each claimed fix against the finding's described **mechanism** (not the
   symptom), and note which fix option was chosen when the finding offered options.
3. Run the relevant test scope once for the whole sweep (e.g., `-Dtest=Watcher*Test`) and cite
   the same result in each finding it covers.
4. Give **every** file a validation blockquote — positive, negative ("NÃO corrigido", with what
   exactly was checked and why the claim does not hold), or partial ("PARCIALMENTE corrigido",
   naming which part remains). Append it on top of any existing entries per the treatment-history
   rule — do not replace earlier verdicts. A fix sweep often *introduces* new instances of a
   documented problem (e.g., new Javadoc referencing a just-removed flag) — record those inside
   the affected finding instead of silently ignoring them.
5. Apply all renames (`-fixed`, `-accepted`) in the same pass.
6. Sync `geral.md` completely, in the same pass: add a "última revalidação" note in the header
   (date, tip, one-line verdict on the claim — confirmed or refuted, naming what remains);
   update every index row annotation; recompute the statistics table; strike through
   (`~~...~~`) test-coverage gaps that were closed, citing the closing commit; update the
   traceability section for previous-analysis findings that the sweep resolved or eliminated.

The deliverable of a revalidation is the explicit verdict on the claim ("10 de 13 resolvidos;
3 permanecem pendentes: ...") — not just updated files. State it in `geral.md` and in the reply.

## geral.md — overall assessment and index

Every analysis directory has a `geral.md` with:

1. **Header**: analyzer, date, scope (branches + commits + worktree analyzed).
2. **Contexto**: what the change under review does, in a paragraph.
3. **Veredito geral**: overall quality judgment, leading with the conclusion; then the handful of findings that matter most before merge, in priority order.
4. **Índice de achados**: table `ID | Arquivo (link) | Severidade | Onde | Resumo`. Annotate resolved/accepted status directly in the Severidade column (e.g., "**Média** — **RESOLVIDO** (data, commit)") so the table alone tells the current state.
5. **Estatísticas**: table by severity × status (Pendentes / Resolvidos / Aceitos).
6. **Cobertura de testes**: gaps observed; strike through (`~~...~~`) gaps that get closed later, noting when.
7. **Rastreabilidade**: mapping to findings from previous analyses (e.g., `.glm-findings/*`) — resolved by design, still open, replicated.

Keep `geral.md` in sync with every finding update — a stale index is worse than none.

## Related skills

- `code-review` — the analysis methodology that produces findings.
- `run-test` — how to run tests when validating a fix.
- `powershell` — command syntax for git/mvnw during validation.
