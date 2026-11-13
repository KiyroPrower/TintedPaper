# DatapackOptimizer native migration

Target: TintedPaper 26.3, fork commit `3ab88076`, Paper
`df120fd8b5211f61146a59f69497a5a62064b917`. DPO source: `6bf15e3` plus the
preserved local recipe compatibility work. The 26.2 production server and
`D:\RuneCraft-local` have not been modified by this migration.

## Current state

This is an incomplete migration, not a replacement release for DPO.

| Active injections | Decision |
| --- | --- |
| 10 | Native behaviour implemented and local regression suite passed |
| 36 | Equivalent mechanism verified in the pinned TintedPaper/Paper baseline |
| 6 | Removed 26.2 noise pipeline or DivineMC-only implementation |
| 405 | Native target found; semantic comparison still pending |
| 1 | External target still needs source resolution |

The source manifests contain 458 active injections. Matching a target file is
only navigation evidence, never proof that its behaviour was ported or superseded.
Remaining helper services, bootstrap work, configuration migration, profiler and
administrative commands must also be reviewed; the mixin count alone is not the
completion criterion.

The machine-readable decisions and source evidence are in
[`dpo-port-decisions.json`](dpo-port-decisions.json). Regenerate the read-only
inventory from the real DPO checkout:

```powershell
python scripts/dpo-port-inventory.py --source ../DatapackOptimizer --summary
```

Omit `--summary` for every qualified injection, source path, target mapping and
review state. Duplicate injections, missing source files and ambiguous/stale
decision entries fail rather than disappearing from the inventory.

## Implemented native batch

- Team saves omit invalid null members while retaining valid members and leaving
  the live team untouched. Normal saves still use the native fast path.
- Tracked scoreboard holders are collected without relying on the fastutil size
  counter. Transient iteration failures have the same bounded recovery policy as
  DPO; persistent failures still propagate. This does not make async scoreboard
  mutation supported or repair the underlying corrupted map.
- Compiled NBT paths use a synchronized, bounded LRU. Only successful parses whose
  cursor matches the scanner boundary are cached. Invalid input is never cached.
- Filtered NBT lists avoid stream pipelines, preserving native behaviour for
  subclassed lists, creation semantics and pattern copies.
- Undefined named holder sets are encoded directly rather than sending a tag key
  absent from the registry. Known tags retain the native representation.
- The implicit selector alive predicate is retained and evaluated after the option
  predicates. It is never removed from the completed predicate list.
- Dirt paths check a piston's carried block, retaining the native solid-block and
  fence-gate rules. Missing piston block entities retain the original fallback.
- Cube shapes reuse three immutable coordinate ranges, initialized before the
  existing Moonrise collision cache. This retains three small range objects per
  shape in exchange for avoiding subsequent coordinate-list allocations.
- Compound-only NBT paths of 2-32 nodes are prepared once and reuse one result
  list during each read. Values are never cached; missing-path errors and mutable
  results are preserved. A changed/branching node falls back to native traversal.
- ZIP resource listings use one immutable, sorted entry index per open archive,
  shared with overlays. Prefix queries restore archive order before native path
  validation and stream creation. Closing releases the index; reopening a changed
  archive rebuilds it. Index construction costs O(N log N) time and O(N) retained
  entry metadata, replacing repeated full scans with prefix lookup and sorting
  only the matching entries. Namespace discovery remains on the native path.

Validation also exposed two inherited issues addressed in isolated patches:
SNBT's grammar shared mutable empty lists/maps between separate parsed documents,
and the AntiXray SDK callback lacked Paper's legacy material reroute. Empty SNBT
containers now belong to each parse, and the callback uses the existing material
conversion helper. The test fixture now initializes Gale alongside Paper, and the
regressions live inside a package selected by the existing test suites.

These controls live in `config/leaf-global.yml`, through the existing native config
module loader:

```yaml
tintedpaper:
  datapacks:
    nbt-path-cache-size: 1024 # 0 disables; maximum 65536
    nbt-list-match-loops: true
    nbt-named-read-plan: true
    zip-pack-index: true
    selector-alive-check-last: true
  network:
    unknown-tag-holder-sets: true
  fixes:
    scoreboard-team-null-member: true
    scoreboard-tracked-players-race: true
    moving-blocks-destroy-path: true
```

The old DPO plugin flags do not control these new native implementations. The
remaining flag-to-config migration is pending. Do not load the old DPO mixins
over the same native changes during rollout.

## Verified exclusions

- `EntitySchedulerTickListIndexedMixin`: the scheduler already uses Moonrise's
  `ReferenceList`, with an identity index and swap removal.
- `EntitySelectorDistanceAabbMixin`: 26.3 already creates an AABB for a bounded
  distance in `EntitySelectorParser.getSelector`. Keep the new native semantics.
- All 21 `pluto.alloc.*EnumValuesMixin` entries: the inherited Paw patch already uses
  `Direction.VALUES` in the corresponding methods.
- `ConnectionLazyExecuteMixin`: despite its DPO package name, this is not a
  DivineMC-specific target. TintedPaper already includes patch 0186 and its native
  `OptimizeNonFlushPacketSending` toggle. The original blanket classification
  of both `divinemc`/region entries as DivineMC-only must not be reused.
- `RegionSinglePhaseMixin`: its target is DivineMC's
  `org.bxteam.divinemc.async.rct.RegionizedChunkTicking`, which is not this fork's
  parallel world/tracker system. Do not attach it to a different ownership model.
- `SequenceRuleMixin`: 26.3 compiles material rules into a `RuleEvaluator[]` and
  traverses that array, so the old list-iterator optimization is already covered.
- The old scalar `NoiseChunk` wrapping/cache/preliminary-surface ports and
  `MaterialRuleList` no longer have their old execution path. Native 26.3 uses
  `DensitySamplerSet` and `DensityBufferPool`; retain that new pipeline.
- `PerlinNoiseSamplerMixin` targets the removed double-valued `ImprovedNoise`.
  The new float-valued `PerlinNoise`/`GradientNoise` already uses Moonrise noise
  acceleration. Copying the old formula would change the 26.3 terrain contract.

- `CubePointRangeMultiplyMixin`: Gale already multiplies by a final reciprocal.
- `AttributeMapInstanceCacheMixin`: TintedPaper uses its indexed attribute map and passes
  the map itself as the modification consumer; no capturing factory is created.
- `EnderManTeleportSkipUnloadedChunkMixin`: the new shared random-teleport path
  already checks chunk presence after world-border clamping and before reads.
- `CommandSourceStackLazyNamesMixin`: 26.3 already uses the shared
  `NamesProvider.FOR_ENTITY` in `withEntity` and resolves names only in getters.

`MovingBlocksDestroyPathMixin` is ported to `PathBlock` with an explicit
`DIRT_PATH` guard. The remaining unresolved target is authlib's external
`YggdrasilMinecraftSessionService`.

## Patch workflow and handoff

The pre-existing patch 0315 for missing recipe references is preserved.
New changes use applied-source Git commits, not edits to patch text:

- Minecraft features 0316 through 0323.
- Paper server regression/compatibility features 0069 through 0076.
- Native TintedPaper configuration source
  `leaf-server/src/main/java/org/dreeam/leaf/config/modules/opt/DatapackOptimizations.java`.

The native config source, this ledger and the inventory script are ordinary root
repository files; they must be included with the generated patches when the owner
commits the batch. The owner authorized committing and publishing the validated
batch on 2026-11-13. Production deployment remains outside this update.

The contributing guide's `rebuildPatches` abbreviation is ambiguous with the
current paperweight version. Use:

```powershell
.\gradlew.bat :leaf-server:rebuildMinecraftPatches :leaf-server:rebuildPaperServerPatches
.\gradlew.bat applyAllPatches
```

Both exact rebuild tasks and complete patch reapplication succeeded. Git identity
must be provided to patch tasks, through repository config or `GIT_AUTHOR_NAME`,
`GIT_AUTHOR_EMAIL`, `GIT_COMMITTER_NAME` and `GIT_COMMITTER_EMAIL`. No global Git
configuration was changed. Do not read or edit applied sources while an apply
task is running: the intermediate tree is Paper before the fork's features are applied.

## Validation

On 2026-11-13 the owner explicitly authorized local compilation and tests during
this migration, overriding the inherited `AGENTS.md` restriction for those tasks.
That authorization persists across batches. Production deployment remains a
separate action. No performance or runtime compatibility claim is made before
the corresponding verification. Twelve JUnit cases were added in the applied Paper
server test tree:

- Null team member recovery and preserved live membership.
- Tracked holders with a deliberately inconsistent fastutil size counter.
- NBT parse cursor offsets and malformed-input/cache isolation.
- Filtered-list read/create semantics and independent pattern copies.
- Early selector tag rejection while still rejecting dead matching entities.
- Undefined/unbound holder-tag direct wire representation.
- Dirt-path survival with carpet, slab, solid block, fence gate and missing piston entity.
- Independence of parsed empty SNBT lists and maps.
- Immutable cube coordinate ranges and Moonrise cache construction.
- Prepared path reads after data mutation, identical missing-path errors, and
  native traversal for indexed/branching paths and dynamically changed nodes.

An additional AntiXray reroute test covers legacy and current materials.

Four `ZipPackIndexTest` regressions cover native ordering/content/prefix parity,
invalid paths, index release and archive reopening, shared overlay ownership and
priority, and a native closed-ZIP failure when a callback closes the pack. All
four passed after complete patch reapplication.

After regenerating and fully reapplying the patches, this command succeeded:

```powershell
.\gradlew.bat :leaf-server:test createLeafclipJar
```

The resulting JUnit XML reports contain 10,184 cases: 10,098 passed, zero failed
and 86 skipped. All twelve `DatapackPortsTest` and all four `ZipPackIndexTest`
cases passed with no skips. The
existing Gradle exclusion for the `Slow` tag remains in effect.

The current distribution artifact is
`leaf-server/build/libs/leaf-leafclip-26.3.local-SNAPSHOT.jar` (28,354,301 bytes),
SHA-256 `78c2783d93a99d7c7d719e0ad196df35740dbd96af77ded9a24138b949785cf0`.
It still uses inherited technical filenames and runtime identifiers. No server startup,
production deployment, TPS/MSPT benchmark or gameplay integration test was run.

Remaining runtime validation: verify
datapack reload, plugin scoreboard operations, equipment packet encoding, config
reload and world-save/restart using a disposable 26.3 copy. Validate plugin and
datapack compatibility separately before touching the 26.2 production world.

Next code groups: complete tag/type selector indexes and lifecycle verification;
command/NBT preparation and caches; function profiler/throttles and timer ownership;
network guards against the 26.3 codec layout; then every fix, engine port and
bootstrap service with a recorded native/covered/not-applicable decision. No
remaining injection has been silently discarded just because its package names a
fork already included by TintedPaper.
