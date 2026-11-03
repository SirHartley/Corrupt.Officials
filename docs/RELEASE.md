# Release

How Corrupt.Officials is built, packaged and published. Nothing here runs unless a release is asked for: no version bumps, rebuilt jars, tags or published releases for ordinary work. Changelog entry rules are in [CHANGELOG.md](CHANGELOG.md); code owners are in [ARCHITECTURE.md](ARCHITECTURE.md).

## Pieces

| Piece | Path | Role |
|---|---|---|
| Changelog | `Changelog` | Source of the version and the release notes. The newest version heading is the release |
| Mod metadata | `mod_info.json` | Version stamped into the ZIP's copy; loads `jars/src.jar` |
| Version file | `corruptofficials.version` | Online Version Checker file on `master`, registered in `data/config/version/version_files.csv`; stamped at packaging, committed back to `master` after a release |
| Release builder | `jars/src/corruptofficials/tools/ReleaseBuild.java` | `build` and `package`, run with `java <file>` |
| Builder checks | `jars/src/corruptofficials/tools/ReleaseBuildChecks.java` | Headless checks of changelog parsing, stamping and file filtering |
| Build libraries | `lib/` | `starfarer.api.jar`, `json.jar`, `log4j-1.2.9.jar`, `lwjgl.jar`, `lwjgl_util.jar` from game `0.98a-RC8` and `LunaLib.jar`, copied from Industrial.Evolution's `lib/` |
| Release jar | `jars/release/CorruptOfficials.jar` | Built by `build`, gitignored, runtime classes only; shipped as `jars/src.jar` |
| Release workflow | `.github/workflows/release.yml` | Manual only: preview without a version, publish with one |

The IDE artifact (`jars/src.jar`, gitignored, from `.idea/artifacts/src_jar.xml`) is separate from the release jar, so an IDE build never replaces it.

The release jar is built in the workflow from `lib/` and never committed, as in Tidy.Panels.

## Branches

Work lands on `dev`. A release is cut from `master` after `dev` is merged into it, because `master` holds the online version file and GitHub only offers a manual workflow in the Actions tab once the file is on the default branch.

## Versions

Versions use `major.minor.letter`, starting at `1.0.a`: the first number for a major release, the second for changes players will notice, the letter for fixes and small patches. Reset the lower parts when a higher one changes. The newest `Changelog` section is marked `- UNRELEASED` until it ships.

## Build the release jar

From the repository root, with JDK 17:

```text
java jars/src/corruptofficials/tools/ReleaseBuild.java build
```

- Compiles every source under `jars/src` against the jars in `lib/`. No game install is needed. `starfarer_obf.jar` is not needed: the code uses only API types.
- Refuses any JDK but 17 and a Maven `json.jar` (the game's `JSONException` is checked).
- Builds into a fresh `out/release-build-*`, keeps only runtime classes (the `tools` package never ships), and replaces the release jar only after a clean compile. The jar must contain `corruptofficials/plugins/ModPlugin.class`.
- After a game or LunaLib update, copy the new jars into `lib/` and update the table above.

## Package

```text
java jars/src/corruptofficials/tools/ReleaseBuild.java package          # preview
java jars/src/corruptofficials/tools/ReleaseBuild.java package 1.0.c    # release
```

- Needs a release jar from `build`.
- Preview packages the newest changelog section.
- Release requires the version (`1.0.c`, no `v`) to equal the newest changelog heading, and that section must not be `UNRELEASED`.
- Ships Git-tracked files only: `mod_info.json`, `corruptofficials.version`, `Changelog`, the LunaLib settings icon `corruptofficials.png` (named by `data/config/LunaSettingsConfig.json`), license and credit files if added, `data/` (`csv json shader ship wpn proj system variant skin`), `graphics/` (`png jpg jpeg`), `sounds/` (`ogg wav`) and the release jar as `jars/src.jar`. Source, `jars/src.url`, `lib/`, docs, IDE, agent and workflow files, and any path under a dot, `unused`, `unedited`, `tools` or `docs` directory are left out.
- Fails if `Changelog`, `mod_info.json`, the version file, the icon, `data/config/LunaSettings.csv` or `data/campaign/market_conditions.csv` is missing, or if a `graphics/corruptofficials/` or `sounds/corruptofficials/` path named in `data/` is not packaged.
- Output in `out/release/`: `Corrupt.Officials-<version>.zip` (one `Corrupt.Officials/` folder, sorted, fixed timestamps), `release-notes.md`, the stamped version file and `version.txt`.
- The stamped `directDownloadURL` uses `Corrupt.Officials-<version>.zip`. Releases up to `1.0.b` were uploaded by hand as `Corrupt.Officials<version>.zip`.

## Publish

1. Finish the newest `Changelog` section and remove `UNRELEASED`.
2. Run `build` and `package <version>` locally as the preview check, then merge `dev` into `master`.
3. Start the Release workflow on `master` with `version` set: Actions tab, Release, Run workflow, or `gh workflow run release.yml --ref master -f version=<version>`.
4. The workflow runs the builder checks, builds the jar, packages, creates the release and its tag on the packaged commit, then commits the stamped `corruptofficials.version` to `master`.
5. Check the release ZIP and notes, and that `master`'s version file names the new version.

The workflow has no push, pull request, tag or schedule trigger. Without a version it only uploads a preview artifact. It refuses to overwrite an existing release, and run on an existing version tag it publishes that tag. It compiles from `lib/` and downloads nothing.

## Maintenance

Update this guide when `ReleaseBuild`, `ReleaseBuildChecks`, `.github/workflows/release.yml`, the version file, the branch setup or the jars in `lib/` change.
