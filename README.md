# TintedPaper

**TintedPaper is an AI-driven Paper fork for Minecraft, focused on native datapack optimizations, performance, vanilla mechanics and stability.**

[![Build TintedPaper](https://img.shields.io/github/actions/workflow/status/KiyroPrower/TintedPaper/build-263.yml?branch=ver%2F26.3&label=build)](https://github.com/KiyroPrower/TintedPaper/actions/workflows/build-263.yml)
[![Minecraft 26.3](https://img.shields.io/badge/Minecraft-26.3-blue)](https://github.com/KiyroPrower/TintedPaper/tree/ver/26.3)

**English** | [中文](public/readme/README_CN.md)

## AI-driven development

AI agents, including Codex and Claude, participate in code analysis, implementation, upstream comparisons and regression testing. Changes remain subject to maintainer review and validation; AI involvement is not evidence of correctness or a measured performance gain.

TintedPaper is migrating [DatapackOptimizer](https://github.com/KiyroPrower/DatapackOptimizer) from a plugin into native server patches. Each port is compared with the current server implementation so existing optimizations are reused and obsolete patches are excluded.

## Current status

- Target: Minecraft **26.3**, with **Java 25**.
- Native ports cover scoreboard recovery, NBT parsing and traversal, selector predicates, packet holder sets, piston/path behavior and collision coordinate caches.
- The DatapackOptimizer migration is **incomplete**. See the [migration report](docs/DPO-MIGRATION.md) and [decision inventory](docs/dpo-port-decisions.json) for validated work and outstanding modules.
- Plugin and datapack compatibility, gameplay behavior and TPS/MSPT gains require runtime validation for the intended workload.

## Builds and support

- [Builds and artifacts](https://github.com/KiyroPrower/TintedPaper/actions/workflows/build-263.yml)
- [Releases](https://github.com/KiyroPrower/TintedPaper/releases)
- [Bug reports and feature requests](https://github.com/KiyroPrower/TintedPaper/issues)
- [Contributing](CONTRIBUTING.md)

CI uploads development artifacts. It does not automatically publish a stable release or deploy a server.

## Build locally

```powershell
.\gradlew.bat applyAllPatches
.\gradlew.bat :leaf-server:test createLeafclipJar
```

On Linux or macOS, use `./gradlew` instead of `.\gradlew.bat`. The inherited build task and module identifiers remain in use for patch-system compatibility. Distribution JARs are generated under `leaf-server/build/libs/`; CI names the downloadable artifact `tintedpaper-26.3`.

## License and upstream credits

TintedPaper builds on [Paper](https://github.com/PaperMC/Paper), the [upstream implementation](https://github.com/Winds-Studio/Leaf), and contributions inherited from Gale, Pufferfish, Purpur, Moonrise, Lithium and other projects identified in patch headers. Their authorship and license notices remain intact.

See [LICENSE.md](LICENSE.md), the [license directory](licenses) and individual patch headers for the applicable terms. Renaming the project does not transfer authorship of upstream work.
