# Architecture

Technical routing for the current implementation, checked against game `0.98a-RC8`. Code paths below are relative to `jars/src/corruptofficials/`; data paths are repository-relative. Search the named owner before adding another implementation.

| Reference | Scope |
|---|---|
| [CLAUDE.md](../CLAUDE.md) | Workflow, build gate and document upkeep |
| [RELEASE.md](RELEASE.md) | Version numbers, branches, release builder, package contents and publishing |
| [CHANGELOG.md](CHANGELOG.md) | Change-note format and upkeep for every implementation task |

## Start here

| Change / symptom | Route |
|---|---|
| Penalty too high, too low or wrong at the cutoff | `CorruptOfficialsConditionPlugin.applyCorruption -> SettingsListener.getInt(CORRUPTION_CUTOFF) / getDouble(CORRUPTION_POW)`; [income penalty](#income-penalty) |
| A colony has no `Government Corruption` line | `ConditionApplicator.reportEconomyTick -> applyResourceConditionToAllMarkets -> CorruptOfficialsConditionPlugin.applyConditionToMarket` (`isInEconomy`, `hasCondition`) -> `applyCorruption` early returns (disabled, no population, income below cutoff) |
| Penalty ignores AoTD income | `AoTDCompat.isEnabled -> findMethod -> getNetIncome`; [AoTD compatibility](#aotd-compatibility) |
| A setting has no effect | `data/config/LunaSettings.csv -> Settings -> SettingsListener.loadSettings / settingsChanged -> cache`; [settings](#settings) |
| Crash or no penalty on a market whose population industry comes from another mod | `CorruptOfficialsConditionPlugin.getPopulation` (id `population`, then any `PopulationAndInfrastructure`) and `unapply` |
| Turning the mod off leaves the penalty | `Settings.CORRUPTION_ENABLED -> applyCorruption` returns before adding the modifier; `unapply` removes it on the next reapply |

## Registration and lifecycle

| Registry / hook | Owners |
|---|---|
| `mod_info.json` | `modPlugin` `corruptofficials.plugins.ModPlugin`; jar `jars/src.jar`; no `dependencies` (LunaLib is needed anyway, see [settings](#settings)); not `utility` |
| `ModPlugin.onApplicationLoad()` | Adds a `SettingsListener` to LunaLib when `lunalib` is enabled, then `SettingsListener.loadSettings()` |
| `ModPlugin.onGameLoad()` | `AoTDCompat.reset()`, then `ConditionApplicator.register()` (transient listener), then `ConditionApplicator.applyResourceConditionToAllMarkets()` |
| `data/campaign/market_conditions.csv` | Condition `corruptofficials_cond` (`Corrupt Officials`), script `corruptofficials.conditions.CorruptOfficialsConditionPlugin`, `decivRemove` `TRUE`, order `1000`. `showIcon()` is false, so it never shows on the planet screen |
| `data/config/LunaSettings.csv` | The three settings on the `General` tab and their help text |
| `data/config/LunaSettingsConfig.json` | LunaLib settings icon for `corruptofficials`: `corruptofficials.png` at the mod root |
| `data/config/version/version_files.csv` | Version Checker entry for `corruptofficials.version` |

Load order: `onApplicationLoad` (settings) -> `onGameLoad`: `AoTDCompat.reset -> ConditionApplicator.register -> applyResourceConditionToAllMarkets`. After that the condition adds itself to new economy markets on every economy tick and on `reportPlayerColonizedPlanet`.

### Settings

`plugins/Settings` owns the setting ids and their types; `data/config/LunaSettings.csv` owns defaults, bounds, help text and tab. `plugins/SettingsListener` fills a static cache: first the CSV defaults through `getMergedSpreadsheetDataForMod("fieldID", ...)`, then, when LunaLib is enabled, the LunaLib values. `settingsChanged` reloads it. Runtime code reads the cache through `getInt`, `getDouble`, `getBoolean`; a missing id reads as `0`, `0.0` or `false`.

| Id | Type | Default | Bounds | Use |
|---|---|---|---|---|
| `corrputofficials_enabled` | Boolean | `TRUE` | | Master switch, read at each `applyCorruption` |
| `corruptofficials_cutoff` | Int | `40000` | 500 to 100000 | Monthly net income where the penalty starts |
| `corruptofficials_pow` | Double | `-0.6` | -2.0 to -0.1 | Exponent of the share of income above the cutoff that is kept |

`SettingsListener` implements LunaLib's `LunaSettingsListener` and imports `LunaSettings`, and `onApplicationLoad` calls `SettingsListener.loadSettings()` whether or not LunaLib is enabled. Loading `SettingsListener` needs LunaLib's classes, so without LunaLib installed the mod fails at application load although `mod_info.json` declares no dependency. A changed setting applies at the market's next condition reapply.

### Build and checks

`tools/ReleaseBuild.java` (`build`, `package`) and `tools/ReleaseBuildChecks.java` compile against `lib/` and never ship in the release jar; they, `lib/` and `.github/workflows/release.yml` are owned by [RELEASE.md](RELEASE.md). There are no other checks.

## Data identity

| Stable id | Display name / use |
|---|---|
| `corruptofficials` | Mod id, LunaLib settings mod id (`SettingsListener.MOD_ID`) |
| `corruptofficials_cond` | Condition `Corrupt Officials`; also the modifier id on population income (`getModId()` returns the condition id), shown as `Government Corruption` |
| `corrputofficials_enabled` | Setting `Enabled`. The typo is the stored LunaLib key; renaming it resets every player's choice |
| `aotd_theory_of_toolbox` | AoTD mod id checked by `AoTDCompat` |

## Source owners

Use `rg --files jars/src/corruptofficials` for the complete inventory.

| File | Owner / connection |
|---|---|
| `plugins/ModPlugin.java` | Lifecycle entry; `log` writes only in dev mode |
| `plugins/Settings.java` | Setting ids and `FieldType` |
| `plugins/SettingsListener.java` | Settings cache, CSV defaults, LunaLib values, LunaLib listener |
| `listener/ConditionApplicator.java` | Transient `PlayerColonizationListener` and `EconomyTickListener` that adds the condition to every economy market |
| `conditions/CorruptOfficialsConditionPlugin.java` | The penalty: reads income, computes it, applies and removes the population income modifier; `applyConditionToMarket`; `getPopulation` |
| `compat/AoTDCompat.java` | Detects AoTD and calls its expected net income through `MethodHandle`s, cached per game load |
| `tools/ReleaseBuild.java`, `tools/ReleaseBuildChecks.java` | Release builder and its checks ([RELEASE.md](RELEASE.md)) |

## Contracts

### Income penalty

Evidence: `Market` and `BaseIndustry` on `0.98a-RC8` from the `starsector-knowledge` skill.

- `Market.reapplyConditions()` calls `unapply` then `apply` on every condition (apply only when surveyed and not suppressed). `Market.advance` reapplies conditions, then industries, when stability changes; the economy step reapplies conditions through `ReapplyConditionsTask`. `unapply` always runs before `apply`, so the modifier never stacks.
- `Market.getNetIncome()` is industry income + export income - industry upkeep - shortage countering cost (when on) - immigration incentive cost (when on), summed live from the industries' stats. Conditions apply before industries, so `applyCorruption` reads industry income from the last industry pass. It removes its own modifier first so it reads income without the penalty.
- `BaseIndustry.applyIncomeAndUpkeep` puts the market income mult (`getIncomeMult()`) on industry income as `ind_stability`. A flat modifier on population income is scaled by it, so `applyCorruption` divides the penalty by `market.getIncomeMult().getModifiedValue()` first.
- Formula: `x = income / cutoff`; nothing below `x = 1`. `kept = x ^ pow` (pow is negative, so 0 to 1); `target = cutoff + (income - cutoff) * kept`; penalty `income - target`, divided by the income mult, added with `modifyFlatAlways(corruptofficials_cond, -penalty, "Government Corruption")` on the population industry's income.
- The condition goes on every market in the economy, NPC markets included. `decivRemove` drops it on decivilization; the next tick does not add it again because the market leaves the economy.
- `unapply` also removes income mult modifiers under `<id>` and `<id>_corruption`, which earlier versions used before the penalty moved to flat population income.
- `getPopulation` takes the industry with id `population`, then the first `PopulationAndInfrastructure` subclass. `unapply` checks that result but then reads `market.getIndustry(Industries.POPULATION)` directly, which is null when only the subclass fallback matched.

### AoTD compatibility

- `AoTDCompat.isEnabled` checks `aotd_theory_of_toolbox`, loads `data.kaysaar.aotd.tot.misc.AoTDToolboxMisc` and looks for a public `getExpectedMonthlyNetIncomeFromMarket(MarketAPI)`. Not verified against AoTD's source; there is no AoTD jar in `lib/`.
- `java.lang.reflect.Method` is blocked for mod code by the script classloader, so `Method` is resolved through the bootstrap loader and its methods are called through `MethodHandle`s; the found method is held as `Object`.
- The result is cached in statics until `reset()` on game load. A failed lookup disables the compat for that load; a failed call logs and falls back to `market.getNetIncome()`.

### Save data

- Every economy market carries `corruptofficials_cond` and its plugin object, so the plugin class name and the condition id must stay. Removing the mod from a running save is not supported (not tested in game).
- `ConditionApplicator` is added as a transient listener on each load and is never saved. `AoTDCompat` and the settings cache are static and never saved.

## Dead or dormant

| Component | State |
|---|---|
| `data/config/settings.json` `ruleCommandPackages` `corruptofficials.rules` | No such package; the mod has no rules commands |
| `Settings.FieldType.STRING`, `SettingsListener.getString` | No setting uses them |
| `ConditionApplicator.reportPlayerAbandonedColony`, `reportEconomyMonthEnd` | Empty |
