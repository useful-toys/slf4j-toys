---
name: git-pull-request
description: 'Guidelines for creating Pull Requests with proper structure and documentation'
---

# Pull Request Guidelines for slf4j-toys

This skill defines the standards for creating Pull Requests in the slf4j-toys project.

## Language

**All Pull Requests MUST be written in English**, including:
- Title
- Description
- Comments
- Documentation

## Title Format

Pull Request titles should be **descriptive and goal-oriented**, NOT in conventional commit format.

### ✅ Good Titles
- `Implement idempotent termination behavior for Meter`
- `Add support for custom time sources in Watcher`
- `Fix memory leak in Reporter context cleanup`
- `Refactor MeterValidator for better testability`

### ❌ Bad Titles
- `feat(meter): implement idempotent termination` (conventional commit - use in commits, not PR titles)
- `Fix bug` (too vague)
- `Update tests` (not descriptive enough)
- `Changes` (meaningless)

## Body Structure

Pull Request descriptions MUST follow this structured format:

### Issue References

**If the PR resolves one or more issues**, reference them at the beginning of the PR description using GitHub's automatic closing keywords:

```markdown
Fixes #123
Resolves #456
Closes #789
```

**Keywords that close issues**:
- `Fixes #issue` - For bug fixes
- `Resolves #issue` - For feature implementations or improvements
- `Closes #issue` - General purpose closing

**Multiple issues**:
```markdown
Fixes #123, #124
Resolves #125
```

**Cross-repository issues**:
```markdown
Fixes useful-toys/slf4j-toys#123
```

**Why reference issues?**
- Automatically closes issues when PR is merged
- Provides traceability between issues and code changes
- Links discussion context to implementation

### Required Sections

#### 1. `## Context`
High-level introduction to the feature area or component being modified.

**Purpose**: Orient reviewers who may not be familiar with this part of the codebase.

**Example**:
```markdown
## Context

The Meter class is a core component of slf4j-toys that tracks operation 
lifecycle (start, progress, termination). Currently, termination methods 
(ok, reject, fail) can be called multiple times on an already stopped 
meter, overwriting the state from the first termination.
```

**With issue reference**:
```markdown
Resolves #42

## Context

The Meter class is a core component of slf4j-toys that tracks operation 
lifecycle (start, progress, termination). Currently, termination methods 
(ok, reject, fail) can be called multiple times on an already stopped 
meter, overwriting the state from the first termination.
```

#### 2. `## Problem`
Clear description of the issue, bug, or limitation being addressed.

**Purpose**: Explain what's wrong with the current implementation.

**Guidelines**:
- Include code examples showing incorrect behavior
- Use ❌ emoji to mark incorrect behavior
- Explain why this is a problem (user impact, correctness, etc.)

**Example**:
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

#### 3. `## Solution`
Detailed explanation of how the problem was solved.

**Purpose**: Document the technical approach and implementation strategy.

**Guidelines**:
- Use numbered or bulleted lists for clarity
- Explain key design decisions
- Mention any patterns or principles applied (e.g., guard clause, factory pattern)
- Bold important class/method names

**Example**:
```markdown
## Solution

Implemented idempotent termination behavior using guard clauses:

1. **MeterValidator.validateStopPrecondition()** now returns `boolean`:
   - Returns `false` when meter already stopped (blocks re-termination)
   - Returns `true` otherwise (allows termination with warnings)

2. **Meter termination methods** check validation result:
   - commonOk(), reject(), fail() call validateStopPrecondition()
   - Early return when meter already stopped
   - Preserves path and state from first termination
```

#### 4. `## API Changes` (if applicable)
Document any changes to public APIs, method signatures, or behavior.

**Purpose**: Alert reviewers and users to breaking changes or new capabilities.

**Guidelines**:
- Use `### Modified Signature` subsection for signature changes
- Use `### Modified Behavior` subsection for behavior changes
- Use `### New API` subsection for new methods/classes
- Show before/after code examples
- Explicitly state backward compatibility status

**Example**:
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

**Backward Compatibility**: Change is backward compatible - does not affect 
client code, only internal behavior.
```

**Note**: Omit this section entirely if there are no API changes.

#### 5. `## Code Changes`
Summary of files modified with quantitative metrics.

**Purpose**: Give reviewers a quick overview of the scope of changes.

**Guidelines**:
- Separate production code from test code
- Include file counts and test counts
- Use bullet points for clarity
- Mention specific test classes updated

**Example**:
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
  - Fixed state assertions
  - Updated event counts
```

#### 6. `## Test Results`
Validation that changes work correctly and don't break existing functionality.

**Purpose**: Demonstrate that the PR is ready to merge.

**Guidelines**:
- State total number of tests executed
- Use ✅ emoji for passing tests
- Mention any new test coverage added
- Confirm backward compatibility if applicable

**Example**:
```markdown
## Test Results

✅ **All 1710 Meter tests pass**

Comprehensive validation confirms:
- Idempotent termination works correctly
- First termination always wins in all scenarios
- No regression in existing functionality
- Backward compatibility maintained
```

#### 7. `## Examples` (optional, but recommended)
Concrete usage examples showing the fix or new feature in action.

**Purpose**: Help reviewers understand practical impact and usage patterns.

**Guidelines**:
- Use multiple scenarios to cover edge cases
- Show before/after comparisons when fixing bugs
- Use ✅ emoji to mark correct behavior
- Use descriptive scenario titles

**Example**:
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

#### 8. `## Relevant Documentation` (optional)
Links to related documentation, TDRs, or implementation plans.

**Purpose**: Provide additional context and traceability.

**Guidelines**:
- Link to TDR documents if architectural decisions were made
- Link to TODO.md or planning documents if they exist
- Mention related PRs or issues

**Example**:
```markdown
## Relevant Documentation

- [TODO.md](https://github.com/useful-toys/slf4j-toys/blob/fix-stop-methods/TODO.md): 
  Implementation plan followed during development
- Test files validate all edge cases (post-stop, pre-start termination)
- Guard clause pattern applied consistently
```

### AI Attribution

**REQUIRED**: Every AI-generated Pull Request MUST end with attribution:

```markdown
---

Co-authored-by: GitHub Copilot using <model name>
```

Where `<model name>` is the specific AI model used (e.g., `Claude Sonnet 4.5`).

## Complete Example

**Without issue reference**:
See [PR #44](https://github.com/useful-toys/slf4j-toys/pull/44) for a complete example following this structure.

**With issue reference**:
```markdown
Fixes #123

## Context

The Meter class is a core component...

## Problem

When a meter is already stopped...

...rest of PR description...

---

Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
```

## Creating Pull Requests

### Using GitHub CLI

**Recommended approach** for complex PR descriptions with code blocks and special characters:

```powershell
# 1. Create PR body in a temporary file
$prBody = @'
Resolves #123

## Context

Your PR description here...

---

Co-authored-by: GitHub Copilot using Claude Sonnet 4.5
'@

$prBody | Out-File -FilePath pr-body.txt -Encoding UTF8

# 2. Create PR using body file
gh pr create --title "Your descriptive title" --body-file pr-body.txt

# 3. Clean up temporary file (optional)
Remove-Item pr-body.txt
```

**Why use body-file?**
- Avoids PowerShell quoting issues with multi-line strings
- Preserves formatting of code blocks
- Handles special characters correctly

### Alternative: Interactive Editor

```powershell
# Opens default editor for PR title and body
gh pr create --editor
```

### Verifying PR Creation

After creating the PR:
1. Visit the PR URL provided by `gh pr create`
2. Verify all sections are properly formatted
3. Check that code blocks render correctly
4. Ensure AI attribution is present at the bottom

## Best Practices

### DO:
- ✅ Use descriptive, goal-oriented titles
- ✅ Reference related issues using closing keywords (Fixes, Resolves, Closes)
- ✅ Follow the structured format consistently
- ✅ Include code examples showing the problem and solution
- ✅ Provide quantitative metrics (test counts, file counts)
- ✅ Confirm all tests pass before creating PR
- ✅ Link to relevant documentation and TDRs
- ✅ Always include AI attribution for AI-generated PRs

### DON'T:
- ❌ Use conventional commit format in PR titles
- ❌ Forget to reference related issues when applicable
- ❌ Skip required sections (Context, Problem, Solution, Code Changes, Test Results)
- ❌ Create PRs without running tests first
- ❌ Mix unrelated changes in a single PR
- ❌ Forget AI attribution
- ❌ Use vague descriptions like "fix bug" or "update code"

## Section Guidelines Reference

| Section | Required? | Purpose |
|---------|-----------|---------|
| Issue References | ⚠️ If applicable | Link to and auto-close related issues |
| Context | ✅ Yes | Introduce the component/feature area |
| Problem | ✅ Yes | Explain what's wrong or missing |
| Solution | ✅ Yes | Describe how it was fixed/implemented |
| API Changes | ⚠️ If applicable | Document public API modifications |
| Code Changes | ✅ Yes | Summarize files and scope |
| Test Results | ✅ Yes | Prove changes work correctly |
| Examples | ✅ Recommended | Show practical usage |
| Relevant Documentation | ⚠️ If applicable | Link to TDRs, plans, related PRs |
| AI Attribution | ✅ Yes | Credit AI assistance |

## Related Documentation

- [git-commit-push skill](../git-commit-push/SKILL.md) - Commit message conventions
- [AGENTS.md](../../../AGENTS.md) - Project AI agent guidelines
- [.github/copilot-instructions.md](../../copilot-instructions.md) - AI attribution standards
