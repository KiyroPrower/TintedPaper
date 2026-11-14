# Parallel world optimization comparison

This is a bounded comparison with Worldthreader
`6fbaef2f424d9a81c9d16617f58b58076c5d045c`, not a general parallel-safety audit.
TintedPaper already has a per-world executor and a tick barrier inherited from
Leaf/SparklyPaper. Its implementation remains the scheduler; a second scheduler
has not been installed. Worldthreader's exclusive-world access, atomic scoreboard,
entity-reference and portal-recovery mechanisms are still pending comparison.

## Explosion-density cache cleared before the tick barrier

**Attribution: Leaf-introduced** - feature patch
`0175-SparklyPaper-Parallel-world-ticking.patch` moves `level.tick` into a submitted
task, leaving the cache clear before waiting for those tasks.

The pinned Paper baseline (`df120fd8b5211f61146a59f69497a5a62064b917`, applied `base`
tag) executes `level.tick(haveTime)` synchronously before clearing the cache.
In the current parallel path, `MinecraftServer.tickChildren` schedules the world
and immediately clears `Level.explosionDensityCache`, while that world's
`ServerExplosion.getBlockDensity` can read/write the same ordinary `HashMap`.
Submission does not make that main-thread clear happen before worker reads or
after worker writes. The final future barrier is too late to protect it.

The fix moves the clear to `MinecraftServer.tickLevel`'s `finally` block and
removes the main-thread clear. Both serial and parallel ticks now clean up on
their tick owner after access, including recovered update suppression and
propagated failures. No map implementation, error propagation, tick scheduling,
chunk ownership check or world-executor default is changed.

Before the fix, both `WorldTickExplosionCacheTest` regressions failed. They
exercise the actual private world-tick entry point with a latch-controlled world
worker and failure paths, checking cleanup timing, clearing-thread identity and
cache lifetime. After the fix, both tests and the local NormalTestSuite passed;
both patch regeneration tasks also passed. This does not reproduce a
production HashMap corruption or attribute the previous entity-tag crash to this
independent issue.

## Unresolved shared hopper state

**Attribution: Leaf-exposed** - Paper's shared flags remain static while Leaf
allows different worlds to execute hopper transfers concurrently.

`HopperBlockEntity.skipHopperEvents`, `skipPullModeEventFire` and
`skipPushModeEventFire` are shared by all worlds. The main tick loop sets the first
flag from each world's configuration before its worker completes; transfer code
also changes the other flags. These paths still need isolated transfer state and
event/cancellation/conservation regressions. No claim is made here that hopper
events or general plugin execution are safe under parallel world ticking.
