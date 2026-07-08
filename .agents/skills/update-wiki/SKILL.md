---
name: update-wiki
description: 'Wiki synchronization workflow for slf4j-toys — cloning, updating, editing, committing, and pushing content to the GitHub wiki. Use whenever planning or coding behavior changes, new features, or architectural modifications that need to be reflected in the project wiki.'
---

# Wiki Synchronization

## Why this skill exists

The project wiki (`https://github.com/useful-toys/slf4j-toys.wiki.git`) lives as a separate Git repository from the main codebase. To keep documentation in sync with code changes, this repo carries a `wiki/` directory that is a full checkout of the wiki repository. Editing wiki pages directly in this working tree alongside code changes makes it straightforward to update documentation as part of the same feature or fix.

We accept that the wiki may occasionally be ahead of the code's `main` branch — a wiki update pushed before a PR merges is acceptable for simplicity.

## When to use

Invoke this skill whenever:
- Planning a behavior change, new feature, or architectural modification that alters what users need to know
- Coding such a change — include wiki updates as part of the implementation, not an afterthought
- The AGENTS.md "Wiki synchronization" guideline triggers

## Guidelines

- The `wiki/` directory is checked out from `https://github.com/useful-toys/slf4j-toys.wiki.git`.
- Always work on the `master` branch of the wiki repository (GitHub wikis use `master` as the default, not `main`).
- Never create branches in the wiki repository — commit directly to `master`.
- The `wiki/` directory is listed in `.gitignore`; its changes do not interfere with the main repository.

## Workflow

### Step 1: Ensure the wiki is available and up to date

```powershell
if (-not (Test-Path -LiteralPath "wiki")) {
    git clone https://github.com/useful-toys/slf4j-toys.wiki.git wiki
} else {
    git -C wiki pull --ff-only origin master
}
```

If the fast-forward pull fails, the local wiki has diverged from the remote — **stop and ask the user** for guidance.

### Step 2: Edit wiki pages

Make changes to the Markdown files under `wiki/`. Follow the existing conventions of the wiki:
- Use GitHub-flavored Markdown
- Use `[[PageName]]` syntax for internal wiki links
- Maintain the existing page structure and formatting style

### Step 3: Stage and commit

Stage the changed wiki files and commit with a descriptive message:

```powershell
git -C wiki add <changed-page.md>
git -C wiki commit -m "Update <PageName>: <summary of change>"
```

Commit messages in the wiki repository are free-form (no Conventional Commits requirement). Keep them clear and descriptive.

### Step 4: Push to update the GitHub wiki

```powershell
git -C wiki push origin master
```

After pushing, the changes are immediately live on the GitHub wiki.

### Step 5: Include the wiki change in the main-repo commit or PR

When committing the code change that motivated the wiki update, mention the wiki change in the commit message body so it's traceable (e.g., "Updated wiki: <PageName>").

## Related skills

- `trunk-based-development` — branch and worktree mechanics for the main repository (not the wiki)
- `git-commit-message` — commit message conventions for the main repository
- `powershell` — PowerShell syntax for the git commands used in this workflow
