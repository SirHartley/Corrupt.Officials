# Changelog guide

How agents write entries in the root `Changelog`. Read it for every implementation task. Version numbers, release steps and how the release tooling reads the file are in [RELEASE.md](RELEASE.md). Wording follows the house-style skill's "Changelog entries" section; this guide does not replace it.

## When to write an entry

- Every implementation task adds or amends its entries in the current unreleased section before merge, in the same commit as the change.
- Read-only work and documentation-only changes add nothing. Say why in the PR when a change needs no entry.
- Check whether the feature has been released: look for it under every version heading without `UNRELEASED`.
  - Not released yet: edit its `Added -` entry so it describes the feature as it now stands. No `Fixed -`, `Adjusted -` or `Removed -` line for it.
  - Released: the change gets its own entry.
- Record only completed changes from the actual diff. Never list plans or unfinished work.

## File layout

`Changelog` at the repository root, plain text, no extension. This mod's file has no `Known issues` or `Notes` header sections; keep that shape and do not add them unless asked. Versions newest first, a blank line between them:

```text
1.0.c - UNRELEASED
Fixed - Crash when a colony's population industry comes from another mod

1.0.b
Fixed - Crash if, for whatever reason, a colony does not have population and infrastructure
```

- Version heading: the version alone on its line, optionally followed by ` - UNRELEASED`. No `v` prefix, no date, no Markdown. The release tooling reads these headings, so keep them in this form.
- Entries: one change per line, starting `Added - `, `Adjusted - `, `Fixed - ` or `Removed - `, in that order.
- A `Note - ` line directly below an entry gives a limit or something the player must do.
- The mod is small: no category lines unless a version gets many entries.
- If no unreleased section exists, add one above the newest version. Ask the user which version comes next if it has not been chosen; do not invent a new version per commit.

## Wording

Entries are player-facing. Short (under 80 characters), a capital letter after the dash, no full stop on a one-clause line, no semicolons, no curly quotes, no hyphen as a dash.

- Name what the player sees: the setting (`Income Cutoff`, `Corruption Power`, `Enabled`), the income line (`Government Corruption`), the colony screen.
- `Added -` names the thing, then what it does: `Added - Setting to turn corruption off mid game`.
- `Adjusted -` gives numbers as old `->` new with the unit: `Adjusted - Default income cutoff 40000 -> 50000 credits`.
- `Fixed -` says what the player saw: `Fixed - Corruption no longer applies while disabled`, `Fixed - Crash when a colony has no population`.
- Cross-mod fixes name the mod (`AoTD`, `LunaLib`).
- Never the call chain or class names. No agent or model names, links, validation logs or commit hashes. No first person, jokes or hedges; untested status goes in the PR.
- Keep the user's own lines verbatim. Never tidy, shorten or merge them unasked.

## Amending versus adding

- Combine entries when several edits fix the same player-visible problem.
- An unreleased feature removed again before release loses its `Added -` entry; it gets no `Removed -` line.
- Never repeat a change once per file, step or PR.

## Maintenance

Update this guide when the `Changelog` layout or entry format changes. Release mechanics belong in [RELEASE.md](RELEASE.md).
