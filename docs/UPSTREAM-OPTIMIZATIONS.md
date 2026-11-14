# Requested optimization ports

Pinned sources inspected on 2026-11-14. This is an incomplete migration; a
repository appearing here does not mean all its optimizations have been ported.
FastNoise was requested twice and is tracked once.

| Source | Pinned revision | State |
| --- | --- | --- |
| [BetterMaps](https://codeberg.org/NerjalNosk/bettermaps) | `a6e7fb7c5c31a1e54ec6ace339d4ca70c71f1488` | Deferred explorer-map integration pending. |
| [Lomka](https://github.com/Starlevka/Lomka) | `5d8bf834d5d3efa783ecad0c3d2fb15a5634550f` | Bulk NBT array stream reads implemented; four local regression tests passed. Other server optimizations pending. |
| [Worldthreader](https://github.com/2No2Name/worldthreader) | `6fbaef2f424d9a81c9d16617f58b58076c5d045c` | Existing parallel world ticking retained. Cross-world ownership and compatibility mechanisms still need comparison. |
| [UUID-Hex](https://github.com/CJDevZ/UUID-Hex) | `e538db835f6fc56809559be6f4d4c6b8bcee20e1` | Datapack function/storage API integration pending. |
| [Explodee](https://github.com/KnownSH/Explodee) | `58b4387177d3c24b434285612b9a5042f81952ff` | Spherical explosion rays pending an opt-in adaptation inside the existing Moonrise explosion loop. |
| [Potatoptimize](https://github.com/Tater-Certified/Potatoptimize) | `cc66d31103eba2e0e24d47a46d0bbc313e686b23` | Sign ticking removal/editor validation already present in CraftBukkit. Other features pending comparison. |
| [Vertigo](https://github.com/Builderb0y/Vertigo) | `7a64414ab9b615f8aeea2d8ce29f2a9290f4cec8` | Negotiated client/server section protocol integration pending. |
| [FastNoise](https://codeberg.org/ZenXArch/FastNoise) | `8e88157ca6366d15f406bc9bab27ef23654f664d` | Noise, surface and material-compiler integration pending comparison with native generation. |

## Lomka NBT stream reads

The native `IntArrayTag` and `LongArrayTag` readers read `DataInputStream`
payloads using at most 8 KiB of temporary byte storage per call, decoding blocks
in big-endian order. Both `load` and streaming `parse` share the same accounted
reader. Non-stream `DataInput` implementations keep their original primitive
loops. The length cap, heap accounting, truncation exceptions and wire format
are unchanged. Empty arrays allocate no scratch storage.

This adapts Lomka's `MixinIntArrayTag` and `MixinLongArrayTag` by Starlev and
contributors under LGPL-3.0-only; see `licenses/LGPL-3.0.txt`. Notices remain in
the native source and generated patch. Moonrise's separate optimized NBT I/O
path remains unchanged; no claim is made that every chunk read uses this path.
Reduced stream calls are verified by tests, not a measured server TPS gain.

## Integration constraints established from source

- BetterMaps workers call structure lookup and publish item/player changes
  directly. Adapt deferred lookup around Paper's world ownership instead of
  copying that worker implementation. Preserve origin, dimension, components,
  loot/trade settings and item consumption.
- Worldthreader has exclusive-world access, atomic scoreboard operations,
  reference snapshots and portal recovery in addition to its scheduler. Existing
  parallel ticking alone does not establish equivalence. Do not install a second
  scheduler alongside Leaf's implementation.
- UUID-Hex is a datapack, not a Java UUID library: callers use `uuid:_` or
  `uuid:convert`, with storage `uuid:in UUID` and output `uuid:out plain`. Any
  specialization must preserve these contracts and datapack overrides.
- Explodee intentionally changes block destruction patterns, neighboring TNT
  ignition and random consumption. Retain Paper cancellation, drops, protected
  blocks, damage and knockback when adapting it.
- Potatoptimize's fixed slime salt/height must not override Paper configuration
  or SecureSeed. Its stat batching increments by 20 after 19 ticks; do not import
  that accounting mismatch or bypass portal collision checks as an invisible
  default change.
- Vertigo requires the client mod and capability negotiation to resynchronize
  sections. Unmodified clients must retain complete native chunks. Preserve
  Anti-Xray, packet-plugin and biome data paths.
- FastNoise includes Fabric section-lock removal redirects. Preserve Moonrise
  ownership, generation callbacks, density extensions, retrogen and seed parity
  while adapting its actual algorithms.

All contributions retain the original authorship and applicable licenses.
