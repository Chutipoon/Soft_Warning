# HANDOFF — Soft_Warning

_2026-07-06 — status-check only, no code touched (see `check/plan-soft-warning.md`)._

## Status: unchanged since 2026-06-12

19 Kotlin files, last commit 2026-06-10. No device test has been recorded
anywhere in this repo (no device model / Android version / yes-no result
found in any `.md` file). This confirms the portfolio plan's premise is
still accurate — nothing has moved since the last review.

## Why nothing was done here this session

The plan for this project is explicit: **the core-loop device test on the
real Samsung phone is the user's own task, not agent work** (an agent has no
physical Android device to test on). Per the plan's anti-goal, no more
scaffolding/libraries/features should be added before that test happens —
so no code changes were made.

## What's still needed (unchanged from the plan)

Build a debug APK, grant usage-access + overlay permissions, open a target
app, and see whether the warning overlay appears after the threshold —
including after the phone sits idle. Record the yes/no result **here or in
CLAUDE.md**, with device model + Android version, once run. See
`check/plan-soft-warning.md` for the full decision fork (continue vs.
archive) that result determines.
