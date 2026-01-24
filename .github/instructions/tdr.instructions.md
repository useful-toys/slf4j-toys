---
description: Guidelines for creating Technical Decision Records (TDRs) in the project.
applyTo: '**/doc/TDR-*.md'
---

# Technical Decision Records (TDRs)

TDRs document important technical and architectural decisions. Use this structure for any significant design decision.

## Structure

All main sections use `##` (level 2 heading). Subsections within Consequences and Alternatives use `###` (level 3 heading).

| Section | Required | Heading | Content |
|---------|----------|---------|---------|
| **Title** | ✅ | `# TDR-XXXX: Description` | Four-digit number + descriptive title |
| **Metadata** | ✅ | (none, bold text) | `**Status**: <value>`<br/>`**Date**: YYYY-MM-DD` (ISO 8601, e.g., 2026-01-23) |
| **Context** | ✅ | `## Context` | Problem, background, constraints (paragraphs + optional lists) |
| **Decision** | ✅ | `## Decision` | Chosen solution and how it works (paragraphs + optional code blocks) |
| **Consequences** | ✅ | `## Consequences` | Three subsections (level 3):<br/>`### Positive ✅`<br/>`### Negative ❌`<br/>`### Neutral ⚖️` (optional)<br/>Each contains bulleted list of items |
| **Alternatives** | ✅ | `## Alternatives Considered` | Each alternative as level 3 heading:<br/>`### ❌ Alternative Name`<br/>Followed by paragraphs explaining description and rejection reason |
| **Implementation** | ⚠️ Optional | `## Implementation` | Brief summary (file structure, commands, configuration) |
| **References** | ⚠️ Optional | `## References` | Bulleted list of links to related TDRs, docs, external resources |

## Valid Status Values

- **Accepted**: Decision is active and implemented
- **Proposed**: Under consideration, not yet finalized
- **Rejected**: Decision was considered but not adopted
- **Superseded**: Replaced by a newer TDR (include reference to new TDR)
- **Deprecated**: No longer recommended but still documented

## Format Examples

### Metadata
```markdown
**Status**: Accepted  
**Date**: 2026-01-23
```

### Consequences Section
```markdown
## Consequences

### Positive ✅

- **Benefit description**: Explanation of advantage
- **Another benefit**: Additional positive outcome

### Negative ❌

- **Trade-off description**: Explanation of downside
- **Limitation**: Constraint or cost

### Neutral ⚖️

- **Observation**: Neither good nor bad, just notable
```

### Alternatives Section
```markdown
## Alternatives Considered

### ❌ Alternative Name

**Description**: How this alternative would work...

**Why rejected**: Specific reasons why not chosen...

### ❌ Another Alternative

**Description**: ...

**Why rejected**: ...
```

## File Naming

- **Pattern**: `TDR-NNNN-short-kebab-case-description.md`
- **Location**: `doc/` folder in repository root
- **Examples**:
  - `TDR-0001-offloading-complexity-to-interfaces.md`
  - `TDR-0031-ide-friendly-build-with-optional-logback-testing.md`

## Key Points

1. **Be explicit about trade-offs**: Negative consequences add credibility
2. **Document alternatives fairly**: Show they were seriously considered
3. **Keep it accessible**: Explain technical concepts without assuming expertise
4. **Link related TDRs**: Use References section to show decision connections
5. **Use concrete examples**: Code blocks, commands, file paths improve clarity

## Minimal Template

```markdown
# TDR-0042: Short Descriptive Title

**Status**: Accepted  
**Date**: 2026-01-23

## Context

Describe the problem, background, and constraints that motivated this decision.

## Decision

Explain the chosen solution and how it works.

## Consequences

### Positive ✅

- **Benefit 1**: Explanation
- **Benefit 2**: Explanation

### Negative ❌

- **Trade-off 1**: Explanation
- **Limitation 1**: Explanation

## Alternatives Considered

### ❌ Alternative Name

**Description**: How this would work.

**Why rejected**: Specific reasons.

## References

- [Related TDR-0001](TDR-0001-related-decision.md)
- [External documentation](https://example.com)
```
