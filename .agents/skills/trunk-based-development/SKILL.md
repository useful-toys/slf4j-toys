---
name: trunk-based-development
description: 'Operational workflow for Trunk-Based Development in slf4j-toys — git worktrees, branch/worktree naming, commit and push gates, rebase-based sync, opening and merging PRs, cleaning up stale branches, and cutting releases. Use at the start of ANY code, test, build-config, or non-exempt documentation change in this repo — before writing the first line — to create the dedicated branch/worktree first (never edit directly on main); do not wait to be asked about git mechanics specifically. Trigger on the request''s intent, not on git-specific wording: "fix this bug/issue/problem", "implement/add this feature", and "improve/refactor this" are all trunk-based-development triggers exactly like "create a branch for X" is — and so is starting to *plan* one of these (a fix, an improvement, a new feature), even before any code is written. This includes Plan Mode: read this skill the moment planning begins, not just before ExitPlanMode — the plan must account for branch/worktree creation as its first step from the outset, not as an afterthought added right before exiting plan mode. Also use before committing or pushing, before rebasing onto main, when opening or merging a PR, when asked for repo/branch status or cleanup, or when cutting a release. Content-only concerns (what a commit message or PR description should say) live in `git-commit-message` and `git-pull-request` instead — this skill is the mechanics.'
---

# Trunk-Based Development with Git Worktrees for slf4j-toys

`main` is the trunk. Every change other than the narrow exceptions in section 7 lands via a short-lived branch, developed in its own git worktree, and merged through a Pull Request. Using a dedicated worktree per branch avoids the context-switch cost of `git checkout` — stale `target/` build output and invalidated IDE caches — since each branch gets its own working directory and build state.

**Apply section 1 before starting the change, not after — including in Plan Mode.** When asked to implement, fix, or refactor something, create the branch and worktree first — don't edit files on `main` and only branch once asked about git mechanics specifically. This also applies the moment planning starts for a fix, improvement, or new feature — read this skill when planning begins, not only once implementation does. When planning the change (Plan Mode), the plan itself should name branch/worktree creation as its first step rather than leaving it implicit; don't wait until exiting plan mode to think about it.

**Asking the user to decide on a git action.** Whenever this skill calls for asking the user to choose between committing, pushing, opening a PR, or starting vs. reusing a branch (e.g. section 1.1), use your tool's structured interactive-choice mechanism instead of a free-text question — Claude Code's `AskUserQuestion` tool, GitHub Copilot's equivalent picker, or whatever the AI tool in use provides — and list the recommended option first.

## 1. Branches and worktrees

### 1.1 Decide whether a new branch is needed

When the user asks for a fix, improvement, or new feature, check the current branch before doing anything else:

- **On `main`:** create a new dedicated branch/worktree (1.2) — never edit directly on `main`.
- **On a non-main branch (feature/fix/etc.):** judge whether the requested change is related to that branch's existing purpose.
  - **Related:** continue on the current branch; no new branch/worktree needed.
  - **Unrelated:** stop and ask the user to choose between continuing on the existing branch (discouraged — mixes unrelated concerns into one branch/PR and complicates review/rollback) or starting a new branch from up-to-date `main` (recommended — sync `main` first per 3.1, then branch per 1.2). Don't decide this silently either way.

### 1.2 Naming and worktree location

Branch names follow `<type>/<short-kebab-description>`, where `<type>` is one of the Conventional Commits types from `git-commit-message` (`feat`, `fix`, `build`, `chore`, `test`, `docs`, `refactor`, `perf`, `ci`, `revert`) — confirmed against this repo's actual merged-PR history (`fix/meter-threadlocal-flaky-dirty-stack`, `feat/guard-clause-meter-start`, `build/update-maven-wrapper`, etc.).

Create the worktree **inside** the main clone, under `.worktrees/<branch-slug>/` (git-ignored), where `<branch-slug>` is the branch name with every `/` replaced by `-`. Not a sibling directory: several AI tools used in this repo — GitHub Copilot in particular — are sandboxed to the folder they were opened in and cannot reach a directory outside it, so a sibling worktree (`../<repo>_<branch-slug>`) is simply inaccessible to them. Nesting under the repo root keeps every tool able to reach it while still giving the branch its own working directory and `target/` (the isolation this section exists for in the first place):

```powershell
# From the main clone, after syncing main (section 3.1)
git worktree add .worktrees/fix-meter-x -b fix/meter-x main
Set-Location .worktrees/fix-meter-x
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

Do this periodically, and mandatorily before every push and after fetch or pull, to keep the branch up-to-date with `main` and avoid future conflicts:

```powershell
Set-Location <worktree>
git fetch origin
git rebase main
```

If conflicts arise that don't resolve trivially, **stop and ask the user** rather than guessing at a resolution.

### 3.3 Check pull requests 

Do this periodically, and mandatorily before every push, to avoid PR conflicts and ensure the branch is up-to-date with `main`:

```powershell
gh pr list --state open
```

If any open PRs exist other for the branch, **alert the user** whether to continue with the push or wait for the PR to be merged first.

### 3.4 Resuming an existing feature/fix branch

When resuming work on an existing branch — picking a PR back up, or continuing a branch from an earlier session — first check whether it already has a worktree under `.worktrees/<branch-slug>/` (1.2):

- **Worktree exists:** use it — `Set-Location .worktrees/<branch-slug>` — and update it there; don't create a second worktree for the same branch.
- **No worktree yet:** create one, checking out the branch into it: `git worktree add .worktrees/<branch-slug> <branch>` (only add `-b` if the branch doesn't exist locally yet — see below).

Then, before continuing work in that worktree, `git fetch origin` and compare local vs. remote:

- **Local branch already checked out in the worktree:** if `origin/<branch>` has commits the local branch doesn't (local is behind), fast-forward the local branch to match the remote first, then continue on it.
- **Only the remote branch exists locally (new worktree checked out from `origin/<branch>`):** if a local copy already existed elsewhere with commits `origin/<branch>` doesn't (local was ahead), use that local state instead of the older remote one — the local copy is the one to keep working from.
- Either way, once the branch is at its most up-to-date state, check whether it also needs rebasing onto the latest `main` (3.2) — fetch and compare against `origin/main` too, don't assume it's already current just because it matches its own remote.

**PRINCIPLE: no `feat`/`fix`/`refactor`/etc. branch should be left behind remote `main`.** Check this every time work resumes on an existing branch, not only right before pushing.

**PRINCIPLE: if local `main` is ahead of `origin/main` (unpushed local commits), warn the user before branching or rebasing off it.** A new branch or rebase created from local `main` would carry commits that don't exist on the remote yet — flag this explicitly rather than silently proceeding, since it's easy to miss and affects every branch created from that point on.

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
git worktree remove .worktrees/<branch-slug>
git branch -D <branch>
```

## 6. Releases

Releases are cut only from `main`'s HEAD, fully automated — never by hand:

1. `create-new-version.yml` (`workflow_dispatch`) runs the full test suite, bumps `pom.xml` to the release version and commits directly to `main`, tags it, bumps `pom.xml` to the next `-SNAPSHOT` and commits again, then pushes commits and tag together.
2. The tag push triggers `release-deploy-version.yml`, which publishes to Maven Central and creates the GitHub Release.

```powershell
gh workflow run create-new-version.yml -f version_increment=patch   # or minor / major
```

**Never create a version tag by hand** (`git tag`, `git push --tags`) and never hand-edit the `<version>` in `pom.xml` for a release, under any circumstance — including the direct-to-main exceptions in section 7. Creating a version number tag is exclusively `create-new-version.yml`'s job; a manual tag can desync the release automation's version detection from what's actually tagged.

## 7. Exceptions: direct commits to main

Only for repository/tooling infrastructure and the project's own AI-facing/onboarding documentation — never application code:

- AI prompts and skills (`.agents/`, `.claude/`, and equivalent AI-config directories)
- Repository configuration (`.gitignore`, `.gitattributes`, similar)
- CI/CD pipeline files (`.github/workflows/*.yml`)
- `README.md` and `AGENTS.md`

Even for these, still sync `main` first (3.1) and still follow `git-commit-message` conventions — the exception is skipping the branch/worktree/PR ceremony, not skipping commit discipline. If a change also touches application code, `pom.xml`, or other documentation (e.g. `doc/`), treat it as a normal change: branch, worktree, PR. This exception never covers version tags or release-version `pom.xml` bumps — see section 6.

## Related skills

- `git-commit-message` — what a commit message should say
- `git-pull-request` — what a PR title/description should say
- `run-test` — which test tier to run and when
- `powershell` — syntax for the git/gh commands used throughout this skill
