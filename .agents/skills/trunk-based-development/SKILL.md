---
name: trunk-based-development
description: 'Operational workflow for Trunk-Based Development in slf4j-toys — git worktrees, branch/worktree naming, commit and push gates, rebase-based sync, opening and merging PRs, cleaning up stale branches, and cutting releases. Use whenever creating a branch or worktree, before committing or pushing, before rebasing onto main, when opening or merging a PR, when asked for repo/branch status or cleanup, or when cutting a release. Content-only concerns (what a commit message or PR description should say) live in `git-commit-message` and `git-pull-request` instead — this skill is the mechanics.'
---

# Trunk-Based Development with Git Worktrees for slf4j-toys

`main` is the trunk. Every change other than the narrow exceptions in section 7 lands via a short-lived branch, developed in its own git worktree, and merged through a Pull Request. Using a dedicated worktree per branch avoids the context-switch cost of `git checkout` — stale `target/` build output and invalidated IDE caches — since each branch gets its own working directory and build state.

## 1. Branches and worktrees

Branch names follow `<type>/<short-kebab-description>`, where `<type>` is one of the Conventional Commits types from `git-commit-message` (`feat`, `fix`, `build`, `chore`, `test`, `docs`, `refactor`, `perf`, `ci`, `revert`) — confirmed against this repo's actual merged-PR history (`fix/meter-threadlocal-flaky-dirty-stack`, `feat/guard-clause-meter-start`, `build/update-maven-wrapper`, etc.).

Create the worktree as a sibling directory of the main clone, named `<main-dir-name>_<branch-slug>`, where `<branch-slug>` is the branch name with every `/` replaced by `-` (a literal `/` in the branch name would otherwise nest the worktree inside a subdirectory instead of creating a sibling):

```powershell
# From the main clone, after syncing main (section 3.1)
git worktree add ../slf4j-toys_fix-meter-x -b fix/meter-x main
Set-Location ../slf4j-toys_fix-meter-x
```

## 2. Local development and quality gates

- **Commit frequently** — one small, complete goal per commit, not one commit per session.
- **Commit message format** — see `git-commit-message`.
- **Every commit is a valid state** — the changed code compiles under Java 21, and the tests relevant to the change pass locally (see `run-test` for picking the right tier).
- **Before pushing, the full suite must be green** — both tiers, not just the one relevant to your change: `.\mvnw test -P slf4j-2.0,with-logback` (1525 tests). This mirrors what CI actually runs (`maven-build-test.yml` always builds with the with-logback profile) and catches a full-suite regression before it costs a CI cycle, not after.

## 3. Continuous sync

### 3.1 Update main — fast-forward only

```powershell
Set-Location <repo-root>
git fetch origin
git pull --ff-only origin main
```

If fast-forward fails, `main` has diverged from `origin/main` — **stop and ask the user**; don't merge or reset to force it through.

### 3.2 Rebase the branch onto main

Do this periodically, and mandatorily before every push:

```powershell
Set-Location <worktree>
git fetch origin
git rebase main
```

If conflicts arise that don't resolve trivially, **stop and ask the user** rather than guessing at a resolution.

## 4. Delivery and Pull Requests

1. Rebase onto the latest `main` (3.2) and confirm the full local suite passes (section 2).
2. Push: `git push -u origin <branch>` the first time; after any rebase, `git push --force-with-lease origin <branch>` (never plain `--force`).
3. Open the PR with title/description per `git-pull-request`.
4. CI (`maven-build-test.yml`) re-validates the push and the PR — compile, full test suite, coverage, and the other checks listed in `AGENTS.md`'s CI/CD Standards section.
5. **Merge with Rebase and Merge** — `gh pr merge <PR> --rebase --delete-branch`. Confirmed from this repo's actual merged-PR history: each commit lands on `main` unchanged, with no merge commit and no squash. Never use "Create a merge commit". Because commits land unchanged, it's each commit's own Conventional Commits header (`git-commit-message`) that ends up in `git log` — not the PR title — so there's no need to reconcile PR title format against a squash-commit subject.

## 5. Cleanup (stale branches)

Periodically — after a delivery, or when asked for repo/branch status — sweep for branches whose remote counterpart is gone:

```powershell
git fetch --prune origin
git branch -vv | Select-String '\[.*: gone\]'
```

Present the matches to the user and ask which to remove. For each one they approve:

```powershell
git worktree remove ../slf4j-toys_<branch-slug>
git branch -D <branch>
```

## 6. Releases

Releases are cut only from `main`'s HEAD, fully automated — never by hand:

1. `create-new-version.yml` (`workflow_dispatch`) runs the full test suite, bumps `pom.xml` to the release version and commits directly to `main`, tags it, bumps `pom.xml` to the next `-SNAPSHOT` and commits again, then pushes commits and tag together.
2. The tag push triggers `release-deploy-version.yml`, which publishes to Maven Central and creates the GitHub Release.

```powershell
gh workflow run create-new-version.yml -f version_increment=patch   # or minor / major
```

Never create a version tag by hand (`git tag`, `git push --tags`) and never hand-edit the `<version>` in `pom.xml` for a release — the workflow owns both.

## 7. Exceptions: direct commits to main

Only for repository/tooling infrastructure — never application code or project documentation:

- AI prompts and skills (`.agents/`, `.claude/`, and equivalent AI-config directories)
- Repository configuration (`.gitignore`, `.gitattributes`, similar)
- CI/CD pipeline files (`.github/workflows/*.yml`)

Even for these, still sync `main` first (3.1) and still follow `git-commit-message` conventions — the exception is skipping the branch/worktree/PR ceremony, not skipping commit discipline. If a change also touches application code, `pom.xml`, or non-trivial documentation, treat it as a normal change: branch, worktree, PR.

## Related skills

- `git-commit-message` — what a commit message should say
- `git-pull-request` — what a PR title/description should say
- `run-test` — which test tier to run and when
- `powershell` — syntax for the git/gh commands used throughout this skill
