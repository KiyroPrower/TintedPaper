# Contributing to TintedPaper

TintedPaper is a personal AI-driven Paper fork maintained for a private Minecraft server. Contributions follow the same requirements: explain the concrete problem, preserve supported behavior and provide relevant validation. Patches remain copyrighted by their respective creators.

## Requirements

- Git and a properly cloned repository.
- Java 25; Gradle can provision a matching toolchain.
- Read the repository's [agent instructions](AGENTS.md) before editing.

## Patch workflow

Create a `<type>/<change>` branch (`feat/`, `fix/`, `perf/`, `port/`, `chore/` or `docs/`; never an
agent name, since Codex and Claude both contribute) from the current `ver/26.3` before editing. Each
coherent implementation batch gets its own pull request into `ver/26.3`; do not
push implementation commits directly to that branch. Keep prior published history.

Apply the complete baseline first:

```powershell
.\gradlew.bat applyAllPatches
```

Edit applied Java source, commit only the intended changes in its applied Git repository, then regenerate patches:

```powershell
.\gradlew.bat :tintedpaper-server:rebuildMinecraftPatches :tintedpaper-server:rebuildPaperServerPatches
```

Minecraft changes belong in the applied repository under `tintedpaper-server/src/minecraft/java`; server implementation and tests belong in `paper-server`. API changes belong in `paper-api` and require matching server changes where appropriate. The API and server Gradle modules are `tintedpaper-api` and `tintedpaper-server`.

Do not edit generated patches directly. Keep applied repositories clean before running `applyAllPatches`, because reapplication recreates their contents. Ordinary configuration source, documentation and migration tools must be committed alongside their generated patches.

After editing either generated module `build.gradle.kts`, run
`./gradlew rebuildPaperSingleFilePatches` before reapplication. Rebuild tasks
normalize inherited filename branding to `tintedpaper-` while preserving patch
numbers, contents, author identities and licenses.

## Validation

When authorized by the owner, run:

```powershell
.\gradlew.bat :tintedpaper-server:test createTintedPaperJar
python scripts/check-distribution.py
```

Report the commands and results actually observed. Distinguish compilation, regression tests, gameplay validation and measured performance. Do not describe an optimization as a measured TPS/MSPT improvement without representative results.

## Pull requests and releases

Open the PR after generating patches and completing local checks. `Build and test`
must pass on its current head before merging. It checks patch reapplication, the
server test suites, API/server manifest versions and the Paperclip launcher.
Use a merge commit to preserve the batch's implementation history.

The same workflow runs after integration and publishes the validated artifact as
an ALPHA prerelease, tagged `26.3-build.N`, with `tintedpaper-26.3.build.N-alpha.jar`
and its SHA-256. `N` is the workflow run number, so skipped numbers are expected.
PR builds upload artifacts but cannot publish releases. Manual dispatch on
`ver/26.3` can retry a build as a new run. Existing release tags are not deleted
or moved, and no workflow deploys the server.

This follows Paper's build-number/channel version scheme. Without `BUILD_NUMBER`,
local builds retain Paper's `26.3.local-SNAPSHOT` development version and are not
published by the release preparation script.

Runtime configuration now uses `config/tintedpaper-global.yml` and
`config/tintedpaper-world-defaults.yml`. Legacy files are copied only when the
corresponding new file is absent; originals are preserved and new settings win.
Internal Java package names remain compatible with inherited plugins and patches.

## Migration and compatibility

Compare incoming ports with the pinned upstream implementation before adding code. Record whether the behavior was ported, is already covered, is inapplicable, or still needs investigation in the [migration inventory](docs/dpo-port-decisions.json).

Preserve error handling, configuration defaults, serialization, API contracts and world/entity ownership. Do not import a threading model from another fork without establishing compatibility. Add focused regressions for new nontrivial behavior.

## Pull requests

Describe the problem, resulting behavior, validation and remaining limitations. Disclose AI participation in the PR template. Preserve upstream author credits, copyright notices and patch licenses; see [LICENSE.md](LICENSE.md).
