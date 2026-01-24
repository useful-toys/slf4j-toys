---
description: 'Documentation conventions and standards for Markdown files in the slf4j-toys project.'
applyTo: '**/doc/*.md, README.md'
---

# Documentation Standards

## Language

**All documentation must be written in English**, including:
- Markdown files (README, guides, TDRs)
- Code comments and examples within documentation
- Commit messages and pull request descriptions
- Issue descriptions and discussion

## No Inventions - All Claims Must Be Verifiable
When writing documentation (guides, TDRs, implementation docs, etc.):

- **All factual statements must be based on**:
  - Actual code in the project
  - Existing project documentation
  - External official documentation (e.g., SLF4J API docs, JUnit 5 docs)

- **Never invent features, APIs, or mechanisms** that don't exist in the codebase

- **If information can be reasonably inferred but not explicitly verified**:
  - Ask the user to confirm before documenting
  - Example: "I see class X uses pattern Y. Should I document this pattern as an established convention?"

- **Example of what NOT to do**:
  - Inventing an annotation that doesn't exist
  - Describing functionality not present in the code
  - Making assumptions about design decisions without supporting evidence

- **Example of correct approach**:
  - Search the codebase for actual implementations
  - Read method Javadoc and comments
  - Document what you find, not what you imagine should exist
  - Ask for clarification if unsure

## Markdown Formatting
- Use ATX-style headings (`#`, `##`, `###`)
- Use fenced code blocks with language specifiers
- Use relative links for internal documentation

## Code Examples
- Always specify language for code blocks
- Ensure examples compile and work
- Keep examples concise but complete

## Synchronization
- When public API changes, update README.md
- Cross-reference related TDRs and documentation