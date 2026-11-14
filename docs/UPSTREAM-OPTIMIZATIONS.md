# Requested optimization ports

Pinned sources inspected on 2026-11-14. This is an incomplete migration; a
repository appearing here does not mean all its optimizations have been ported.
FastNoise was requested twice and is tracked once.

| Source | Pinned revision | State |
| --- | --- | --- |
| [BetterMaps](https://codeberg.org/NerjalNosk/bettermaps) | `a6e7fb7c5c31a1e54ec6ace339d4ca70c71f1488` | Opt-in deferred explorer-map integration implemented; five local regressions passed. Async/dynamic lookup and arbitrary request parameters remain pending. |
| [Lomka](https://github.com/Starlevka/Lomka) | `5d8bf834d5d3efa783ecad0c3d2fb15a5634550f` | Bulk NBT array stream reads implemented; four local regression tests passed. Other server optimizations pending. |
| [Worldthreader](https://github.com/2No2Name/worldthreader) | `6fbaef2f424d9a81c9d16617f58b58076c5d045c` | Existing parallel world ticking retained. A cache ownership race found during comparison is fixed and regression-tested; see PARALLEL-WORLD-PORT.md. Cross-world exclusivity and remaining compatibility mechanisms are pending. |
| [UUID-Hex](https://github.com/CJDevZ/UUID-Hex) | `e538db835f6fc56809559be6f4d4c6b8bcee20e1` | Original functions bundled as an optional built-in datapack; three local regressions passed. Live command/storage execution remains untested. |
| [Explodee](https://github.com/KnownSH/Explodee) | `58b4387177d3c24b434285612b9a5042f81952ff` | Spherical ray distribution implemented inside Moonrise's loop, opt-in and default false. Two local regressions passed. |
| [Potatoptimize](https://github.com/Tater-Certified/Potatoptimize) | `cc66d31103eba2e0e24d47a46d0bbc313e686b23` | Sign ticking/editor validation and Halloween caching already present. Christmas caching implemented with two local regressions. Slime, navigation, portals and stat policies remain pending. |
| [Vertigo](https://github.com/Builderb0y/Vertigo) | `7a64414ab9b615f8aeea2d8ce29f2a9290f4cec8` | Negotiated client/server section protocol integration pending. |
| [FastNoise](https://codeberg.org/ZenXArch/FastNoise) | `8e88157ca6366d15f406bc9bab27ef23654f664d` | Fixed-biome filling and End chunk-center result reuse adapted to native palettes/cache. Noise filling, surface and material compiler remain pending. |

## Lomka NBT stream reads

The native `IntArrayTag` and `LongArrayTag` readers read `DataInputStream`
payloads using at most 8 KiB of temporary byte storage per call, decoding blocks
in big-endian order. Both `load` and streaming `parse` share the same accounted
reader. Non-stream `DataInput` implementations keep their original primitive
loops. The length cap, heap accounting, truncation exceptions and wire format
are unchanged. Empty arrays allocate no scratch storage.

This adapts Lomka's `MixinIntArrayTag` and `MixinLongArrayTag` by Starlev and
contributors under LGPL-3.0-only; see `licenses/LGPL-3.0.txt`. Notices remain in
the native source and generated patch, with the original copyright recorded in
`licenses/Lomka-NOTICE.txt`. Moonrise's separate optimized NBT I/O
path remains unchanged; no claim is made that every chunk read uses this path.
Reduced stream calls are verified by tests, not a measured server TPS gain.

## Explodee and holiday caches

`performance.explosions.spherical-rays` defaults to false. Enabling it uses
Explodee's spherical distribution and ray-count formula. The common 100-ray
case has precomputed immutable directions; other counts compute each ray into
one three-double scratch array per explosion, without an unbounded radius cache
or per-ray allocation. The existing resistance cache, block protection, piston,
event, drop, entity-damage and knockback code remains in place. Non-finite radii
retain the native path. Tests compare the source distribution, verify unit
vectors and exercise the real native loop: radius four uses 100 rays when enabled
and 1,352 when disabled. This is a work-count assertion, not a TPS benchmark.
Actual destruction patterns, TNT chains and event-heavy gameplay remain untested.
The ray count grows with radius squared and can exceed the native count for large
explosions; this option is not a universal reduction in work.
Original MIT-0 license: `licenses/Explodee-MIT-0.txt`.

Christmas checks cache one immutable local-day result instead of creating a date
on every call. The independent native adaptation of Potatoptimize's holiday-cache
idea retains the native December 24-26 range, current system timezone and clock
rewinds. Concurrent calls use their own calculated result even if another thread
publishes a different day or timezone. Tests cover boundary milliseconds, year
changes, rewinds and concurrent timezone probes. Halloween retains the existing
Gale implementation. No general world-thread compatibility is implied.

## BetterMaps deferred lookup

`performance.maps.defer-explorer-map-search` defaults to false. Eligible loot
and cartographer maps retain a serialized request in their custom data and appear
as empty maps until used. The request retains the source dimension, original
block position, native function parameters and villager context. Lookup, map
creation and preview rendering happen on the owning world thread at use time.
This moves the work out of unopened/unused loot and offers; it does not remove
the possible lookup pause on the use tick or implement the upstream worker model.

Native Paper treasure-map controls, structure claiming, usage stat, sound,
`PlayerMapFilledEvent` output replacement and inventory/drop handling remain in
their native paths. A failed lookup, wrong dimension or malformed request does
not consume the token. Existing tokens still resolve after disabling creation.
Other item components and unrelated custom data are retained; ordinary maps take
the native path. Recognized tokens receive a normal 20-tick item cooldown.

Client-held custom data can be forged by creative clients. Only named structure
sets, zoom 0-4, search radius 0-50 and origins within the current border are
accepted. Unsupported server-side functions fall back to normal creation instead
of issuing an unusable token. Loot conditions are checked at generation and are
not serialized or rerun on use. Broader client request parameters require
authenticated metadata first. Original MIT notice: `licenses/BetterMaps-MIT.txt`.
Five regressions cover native loot deferral, item-codec persistence, original
lookup parameters/components, failed and wrong-dimension use without consumption,
forged metadata bounds, Paper villager discovery/claim settings, and the native
map-use path firing the fill event and honoring its replacement item. Event and
map-storage adapters are isolated with mocks; live plugins and gameplay have not
been tested.

## UUID-Hex built-in datapack

The original MIT-licensed functions and lookup table are available through
`/datapack enable "tintedpaper_uuid_hex"`. The pack is disabled by default and
does not identify itself to clients as known vanilla data. Enabling it loads the
upstream `uuid` objective and storage table. Its entry points remain `uuid:_`
and `uuid:convert`, input `uuid:in UUID`, and output `uuid:out plain`. It does not
rewrite existing datapacks or replace Java UUID serialization. Avoid enabling
another copy of the same namespace at the same time.

The current singular `function` paths are bundled, without legacy duplicate
directories. Tests verify pack discovery, compatibility, explicit activation,
unchanged function checksums, native command/macro compilation, all 65,536 lookup
entries and signed-word conversion against canonical UUIDs. These checks do not
execute the functions against a running server's scoreboard and storage. The
original license is included inside the built-in pack.

## FastNoise biome shortcuts

An exact native `FixedBiomeSource` fills a section through its existing
single-value palette after one sample instead of 64. Each section receives its
own mutable palette. Subclasses, decorated resolvers, biome edits and custom
resolvers keep native traversal and sampling order. Blending, retrogen, chunk
ownership, block-state palettes and network encoding remain on their existing
paths; no section locks are removed.

The existing opt-in End biome cache now keys X/Z by chunk because the native
sampler always uses the chunk center for those axes. It retains every quart Y,
sampler identity, thread isolation, capacity bound and unpackable-coordinate
fallback. A populated outer-End section needs four erosion samples instead of
64 when this cache is enabled. This applies FastNoise's observation about
horizontal invariance without sharing mutable palette storage or adding a
second cache. These counts describe work verified by regressions, not measured
TPS/MSPT gains or a full generation-parity benchmark.

Tests cover byte-identical fixed-biome section serialization, independent
palettes, overridden sources, custom resolver order, center-island boundaries,
negative chunks and multiple Y ranges against the uncached End resolver. The
existing sampler/thread/capacity tests remain in place. Full-world generation,
custom generation plugins and client gameplay have not been run.

These are independent native adaptations of the biome shortcuts in FastNoise's
`FastBiomeGen` by ZenXArch and contributors. The remaining noise, surface,
material-compiler and negotiated section-protocol work is not included.

## Other integration constraints

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
