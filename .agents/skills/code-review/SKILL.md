---
name: code-review
description: 'Systematic code review and security audit methodology for slf4j-toys — analyzing Java packages for correctness, security, concurrency, design, performance, and style issues, and documenting findings as individual Markdown files under .findings/. Use whenever asked to review, audit, or analyze code for bugs, vulnerabilities, or quality issues. Covers finding categories and prefixes, file naming, per-finding Markdown structure, README index, and a principle-driven investigation approach that adapts to any code under review.'
---

# Code Review and Security Audit for slf4j-toys

Systematic methodology for reviewing Java code in this repository for correctness, security, design, and style issues. Each finding is documented as an individual Markdown file so results are traceable, reviewable, and diffable.

## When to use

- User asks to "analyze", "review", "audit", or "check correctness/security" of a package, class, or module
- User asks to document findings, issues, or vulnerabilities
- Pre-merge or pre-release code quality review
- Security-focused review of configuration parsing, I/O, or logging code

## Process

### 1. Scope and locate

Identify the target scope (package, class, or module). Use `file_search` with a glob like `**/<package>/**/*.java` to find all source and test files. Read every main source file completely — do not skip or skim. Read the corresponding test files to understand intended behavior and coverage gaps.

### 2. Read project conventions

Read the relevant instruction files before judging compliance:

- `.github/instructions/java.instructions.md` — Java coding standards (English-only, immutability, Javadoc, nullability, Lombok, AI attribution)
- `.github/instructions/java-test.instructions.md` — Test conventions
- `.github/instructions/documentation.instructions.md` — Documentation standards
- `AGENTS.md` — Project overview, build profiles, CI/CD standards

Only load instruction files whose `applyTo` pattern matches the files under review.

### 3. Principle-driven analysis

Do not follow a fixed checklist. Instead, apply the investigative principles below to every method, field, and class in scope. Think creatively — the categories and questions are starting points, not boundaries. Any issue you can trace to concrete code is a valid finding, regardless of whether it fits neatly into a category.

#### Think in dimensions

For every unit of code under review (method, field, class, interaction), consider it across these dimensions. The questions are illustrative, not exhaustive — use them to prime your thinking, then follow the code wherever it leads.

**Correctness** — Does the code do what it claims? Trace every code path, including edge cases. Consider: numeric overflow and truncation, floating-point precision and special values (NaN, Infinity), boundary and off-by-one errors, empty/null/negative/zero inputs, type safety and casting, control flow (switch fall-through, short-circuit evaluation, operator precedence), string encoding and locale sensitivity, collection contracts (equals/hashCode, comparator, concurrent modification), date/time timezone and thread safety, serialization invariants.

**Security** — Can an attacker make the code do something unintended? Consider: injection (SQL, command, XML, log, CRLF, regex/ReDoS), authentication and authorization bypass, information disclosure in error messages or logs, sensitive data exposure, path traversal, SSRF, unsafe deserialization, weak cryptography, insecure defaults, hardcoded credentials. Include CWE IDs where applicable.

**Concurrency** — Is the code safe under simultaneous access? Consider: race conditions (check-then-act, read-modify-write), visibility and ordering (missing `volatile`, `final` field publication), deadlock (lock ordering, `synchronized` on shared objects), lock management (missing `try-finally` for `Lock`), thread and executor lifecycle (not shutdown, unbounded pools, `ThreadLocal` leaks), atomicity of compound operations on synchronized collections.

**Design** — Is the code maintainable and extensible? Consider: SOLID violations, coupling and cohesion, mutability of public state, mutable returns from getters, mutable parameters stored without copying, static mutable state, API surface (too broad, too narrow, inconsistent), error handling strategy consistency, hardcoded limits, missing extension points.

**Performance** — Does the code waste resources? Consider: algorithmic complexity (nested loops, linear search where hashing would do), unnecessary object creation, memory retention (unbounded caches, static collections), unbuffered I/O, regex recompilation, lock contention, excessive synchronization on hot paths.

**Robustness** — Can the code fail gracefully? Consider: missing input validation, missing defensive copying, partial failure leaving inconsistent state, missing timeouts, missing fallback strategy, resource exhaustion (unbounded collections, caches without eviction).

**Style** — Does the code follow conventions? Consider: naming conventions (camelCase, `UPPER_SNAKE_CASE` for constants), non-English identifiers (project requires English-only), `final` usage, `@NonNull` usage per project policy, `@SuppressWarnings` hiding real issues, Lombok misuse, method/class complexity, dead code, comment quality.

**Documentation** — Does the code document its contract? Consider: undocumented side effects (trimming, normalization, fallback), ambiguous contracts (inclusive vs exclusive, null vs empty), missing `@throws`, stale Javadoc, missing thread safety or nullability documentation.

**Accepted** — Does the Javadoc or comments explicitly acknowledge a known limitation or design trade-off? Only classify as `accepted` if the code itself documents the issue as accepted — do not infer acceptance.

#### How to think, not what to find

- **Trace data flow**: follow values from origin to destination. Where do they come from? Can they be null, empty, negative, infinite, very large, very small, attacker-controlled?
- **Trace control flow**: walk every branch, loop, and exception path. What happens in the edge case? What happens if the loop runs zero times? What happens if the exception is thrown midway?
- **Question assumptions**: every line of code makes assumptions (non-null, non-empty, in-range, single-threaded, finite, valid format). List the assumptions and check each one.
- **Compare with the codebase**: search for how the method is called, what patterns other classes follow, what the tests cover. Inconsistency with the rest of the codebase is a finding.
- **Think adversarially**: for security, think like an attacker. For robustness, think like a stressed system. For concurrency, think like a scheduler trying to break things.
- **Consider the future**: what happens when a constant changes, a new unit is added, a method is called from a new context, a field becomes mutable?

### 4. Cross-reference with the codebase

Before finalizing a finding, check whether the issue is mitigated elsewhere:

- Search for how the method is called (`grep_search` for the method name across `src/main/java`)
- Check if other classes in the project follow a different pattern (e.g., `String.format(Locale.US, ...)` in JSON formatters vs `String.format(...)` in the class under review)
- Check if tests cover the edge case (tests may mask the issue with annotations like `@WithLocale`)
- Check if `SessionConfig` or other configuration classes set up context that mitigates the issue

### 5. Document findings

#### Directory structure

```
.findings/<analyzer>/<package>/
    README.md
    <prefix>-<number>-<slug>.md
    ...
```

- `<analyzer>` — the tool or agent that produced the findings (e.g., `glm`, `qodana`, `codeql`)
- `<package>` — the Java package name without `org.usefultoys.slf4j.` prefix (e.g., `utils`, `meter`, `watcher`)

#### File naming

```
<prefix>-<number>-<slug>.md
```

- `prefix` — one of the prefixes below
- `number` — zero-padded sequential number within each prefix, starting at `001`
- `slug` — short kebab-case description of the issue

#### Prefixes

| Prefix | Category | When to use |
|--------|----------|-------------|
| `bug` | Erro de implementação | Behavior that is incorrect, unexpected, or silently wrong |
| `sec` | Segurança | Vulnerability or information exposure (include CWE ID when applicable) |
| `accepted` | Aceito | Issue explicitly acknowledged in Javadoc/comments as a known limitation |
| `design` | Design | Design decision with negative impact on maintainability or robustness |
| `style` | Estilo | Violation of coding conventions or naming standards |
| `robust` | Robustez | Fragility to invalid input or future changes |
| `doc` | Documentação | Undocumented or ambiguous behavior |
| `perf` | Performance | Unnecessary resource usage or algorithmic inefficiency |

Propose additional prefixes only when none of the above fit — document the new prefix in the README index.

#### Per-finding file template

```markdown
# <PREFIX>-<NUMBER>: <Title>

| Atributo | Valor |
|----------|-------|
| **Severidade** | Alta / Média / Baixa |
| **Tipo** | <category description> |
| **Arquivo** | <relative path from repo root> |
| **Linhas** | <comma-separated line numbers> |
| **Prefixo** | `<prefix>` |
| **CWE** | <CWE-ID and name, only for sec findings> |

## Descrição

<What is wrong and why. Be specific about the mechanism — trace through the code path that triggers the issue.>

## Exemplo de Reprodução

<Optional: code snippet or scenario that triggers the issue. Omit if the description is sufficient.>

## Impacto

<What happens if the issue is triggered — concrete consequences, not abstract risk.>

## Código Afetado

```java
// The exact lines that are problematic, with enough context to locate them
```

## Recomendação

<Concrete fix suggestion with code. Note Java 8 compatibility constraints if relevant.>

> **Nota**: <Optional caveats — e.g., "tests mask this issue with @WithLocale", "this is a conscious trade-off", etc.>
```

#### README index template

```markdown
# Achados de Análise — Package `<package>`

<One-paragraph summary of what was analyzed.>

## Resumo

| ID | Prefixo | Severidade | Arquivo | Descrição |
|----|---------|------------|---------|-----------|
| [PREFIX-001](prefix-001-slug.md) | `prefix` | Alta/Média/Baixa | <Class> | <one-line description> |
| ... | ... | ... | ... | ... |

## Prefixos Utilizados

| Prefixo | Tipo | Descrição |
|---------|------|-----------|
| `bug` | Erro de implementação | ... |
| ... | ... | ... |

## Estatísticas

| Severidade | Quantidade |
|------------|------------|
| Alta | N |
| Média | N |
| Baixa | N |
| **Total** | **N** |

## Cobertura de Testes

<Bullet list of test coverage gaps relevant to the findings — what the existing tests don't cover.>
```

### 6. Language for findings

Finding files are analysis deliverables for the user, not project source code or official documentation (`doc/*.md`, `README.md`). Write them in the user's locale (pt-br for this project's default user). The `documentation.instructions.md` English-only rule applies to `**/doc/*.md` and `README.md` only — `.findings/` files are exempt.

## Severity guidelines

| Severity | Criteria |
|----------|----------|
| **Alta** | Crash, data corruption, security breach, or incorrect behavior in normal usage |
| **Média** | Incorrect behavior in edge cases, silent data loss, or inconsistency with project patterns |
| **Baixa** | Style violation, documentation gap, robustness concern unlikely in practice, or design smell with workaround |

## Tips

- Read the **entire** source file before writing findings — context from later methods can invalidate early hypotheses.
- Always check **how the method is called** before classifying a finding as high severity — a package-private method called only with valid inputs is lower risk than a public API method.
- Search for **established patterns** in the codebase: if other classes use `Locale.US` in `String.format`, the absence of it in the class under review is a real inconsistency, not a theoretical concern.
- Check **test annotations** — `@WithLocale`, `@ValidateCharset`, `@ResetSystemProperty` can mask production issues.
- **Don't invent** issues — every finding must reference concrete code with line numbers. If you can't point to the line, it's not a finding.
- **Don't over-report** — if a method is package-private, only called internally with valid inputs, and the invariant is clear from context, a missing validation is low-severity robustness at most, not a bug.
- **Don't limit yourself** — the dimensions and questions above are starting points. If you notice something that doesn't fit any category but is still wrong, document it and propose a new prefix if needed.

## Related skills

- `run-test` — verifying whether a finding is reproducible by running the relevant test class
- `git-commit-message` — commit format if findings are committed
- `trunk-based-development` — branch/worktree creation if findings are committed as code changes
