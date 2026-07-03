# GitHub Copilot Instructions

Follow all guidelines specified in [AGENTS.md](../AGENTS.md).

## Shell

Every command in this repo runs in PowerShell on Windows, not bash — this applies regardless of
whether you load `AGENTS.md` or `.agents/skills/powershell/SKILL.md`. Most common failure: chaining
commands with `&&`/`||` is a PowerShell parse error, use `;` instead. Also: quote `-D`/`-P` Maven
arguments containing `#`/`@`/`=` in single quotes; there is no `ls`/`grep`/`tail`/`export`
(`Get-ChildItem`/`Select-String`/`Get-Content -Tail`/`$env:NAME` instead).


## Attribution Override

For code generation attribution, use:
- **AI Assistant Name**: `GitHub Copilot`
- **Javadoc format**: `@author Co-authored-by: GitHub Copilot using <model name>`
- **Commit format**: `Co-authored-by: GitHub Copilot using <model name>`

Where `<model name>` is the specific model being used (e.g., `Claude Sonnet 4.5`, `GPT-4`, etc.).
