# Contributing to TintedPaper

TintedPaper is a personal AI-driven Paper fork maintained for a private Minecraft server. Contributions follow the same requirements: explain the concrete problem, preserve supported behavior and provide relevant validation. Patches remain copyrighted by their respective creators.

## Requirements

- Git and a properly cloned repository.
- Java 25; Gradle can provision a matching toolchain.
- Read the repository's [agent instructions](AGENTS.md) before editing.

## Patch workflow

Apply the complete baseline first:

```powershell
.\gradlew.bat applyAllPatches
```

Edit applied Java source, commit only the intended changes in its applied Git repository, then regenerate patches:

```powershell
.\gradlew.bat :leaf-server:rebuildMinecraftPatches :leaf-server:rebuildPaperServerPatches
```

Minecraft changes belong in the applied repository under `leaf-server/src/minecraft/java`; server implementation and tests belong in `paper-server`. API changes belong in `paper-api` and require matching server changes where appropriate. The build retains inherited technical directory and task identifiers.

Do not edit generated patches directly. Keep applied repositories clean before running `applyAllPatches`, because reapplication recreates their contents. Ordinary configuration source, documentation and migration tools must be committed alongside their generated patches.

## Validation

When authorized by the owner, run:

```powershell
.\gradlew.bat :leaf-server:test createLeafclipJar
```

Report the commands and results actually observed. Distinguish compilation, regression tests, gameplay validation and measured performance. Do not describe an optimization as a measured TPS/MSPT improvement without representative results.

## Migration and compatibility

Compare incoming ports with the pinned upstream implementation before adding code. Record whether the behavior was ported, is already covered, is inapplicable, or still needs investigation in the [migration inventory](docs/dpo-port-decisions.json).

Preserve error handling, configuration defaults, serialization, API contracts and world/entity ownership. Do not import a threading model from another fork without establishing compatibility. Add focused regressions for new nontrivial behavior.

## Pull requests

Describe the problem, resulting behavior, validation and remaining limitations. Disclose AI participation in the PR template. Preserve upstream author credits, copyright notices and patch licenses; see [LICENSE.md](LICENSE.md).
