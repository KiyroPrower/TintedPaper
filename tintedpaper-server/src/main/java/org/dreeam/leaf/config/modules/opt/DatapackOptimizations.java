package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModule;

public final class DatapackOptimizations extends ConfigModule {
    public static volatile int nbtPathCacheSize = 1024;
    public static volatile boolean nbtListMatchLoops = true;
    public static volatile boolean nbtNamedReadPlan = true;
    public static volatile boolean zipPackIndex = true;
    public static volatile boolean unknownTagHolderSets = true;
    public static volatile boolean selectorAliveCheckLast = true;
    public static volatile boolean selectorTagIndex = true;
    public static volatile boolean selectorPredicateLookup = true;
    public static volatile boolean scoreboardTeamNullMember = true;
    public static volatile boolean scoreboardTrackedPlayersRace = true;
    public static volatile boolean movingBlocksDestroyPath = true;
    public static volatile boolean noteBlockPlacePowered = false;
    public static volatile boolean fishSchoolLeader = true;
    public static volatile boolean ghastIdleDirection = true;
    public static volatile boolean observerPlaceShapeUpdate = false;
    public static volatile boolean targetPlaceShapeUpdate = false;
    public static volatile boolean spongeAbsorbUpdate = false;

    @Override
    public void onLoaded() {
        nbtPathCacheSize = Math.clamp(globalConfig.getInt("tintedpaper.datapacks.nbt-path-cache-size", 1024,
            "Maximum shared compiled NBT paths. Set to 0 to disable; bounded to 65536."), 0, 65536);
        nbtListMatchLoops = globalConfig.getBoolean("tintedpaper.datapacks.nbt-list-match-loops", true,
            "Avoid stream allocations while matching NBT list elements.");
        nbtNamedReadPlan = globalConfig.getBoolean("tintedpaper.datapacks.nbt-named-read-plan", true,
            "Reuse traversal scratch space for compound-only NBT paths; always read current tag values.");
        zipPackIndex = globalConfig.getBoolean("tintedpaper.datapacks.zip-pack-index", true,
            "Index ZIP resource paths once per open archive; share the index with overlays and release it on close.");
        unknownTagHolderSets = globalConfig.getBoolean("tintedpaper.network.unknown-tag-holder-sets", true,
            "Encode undefined named holder sets directly instead of sending unknown tag names to clients.");
        selectorAliveCheckLast = globalConfig.getBoolean("tintedpaper.datapacks.selector-alive-check-last", true,
            "Evaluate selector option predicates before the implicit alive check.");
        selectorTagIndex = globalConfig.getBoolean("tintedpaper.datapacks.selector-tag-index", true,
            "Serve @e[tag=...] selectors without a distance or box from a per-level index of tagged entities instead of scanning every entity. Every candidate is still checked by the full selector.");
        selectorPredicateLookup = globalConfig.getBoolean("tintedpaper.datapacks.selector-predicate-lookup", true,
            "Reuse predicate holder lookups per selector and registry generation; entity tests and loot contexts remain live.");
        scoreboardTeamNullMember = globalConfig.getBoolean("tintedpaper.fixes.scoreboard-team-null-member", true,
            "Omit invalid null team members when saving the scoreboard.");
        scoreboardTrackedPlayersRace = globalConfig.getBoolean("tintedpaper.fixes.scoreboard-tracked-players-race", true,
            "Collect tracked score holders without trusting a corrupted collection size counter.");
        movingBlocksDestroyPath = globalConfig.getBoolean("tintedpaper.fixes.moving-blocks-destroy-path", true,
            "Check the block carried by a piston above dirt paths instead of its moving placeholder.");
        noteBlockPlacePowered = globalConfig.getBoolean("tintedpaper.fixes.note-block-place-powered", false,
            "Initialize note block power on placement; changes redstone behavior and respects Paper's disabled note block updates.");
        fishSchoolLeader = globalConfig.getBoolean("tintedpaper.fixes.fish-school-leader", true,
            "Prevent existing fish school leaders from being recruited into another school.");
        ghastIdleDirection = globalConfig.getBoolean("tintedpaper.fixes.ghast-idle-direction", true,
            "Preserve idle ghast heading below 0.1 blocks per tick of horizontal movement, without an attack target.");
        observerPlaceShapeUpdate = globalConfig.getBoolean("tintedpaper.fixes.observer-place-shape-update", false,
            "Send shape updates after a powered observer resets on placement; changes redstone behavior.");
        targetPlaceShapeUpdate = globalConfig.getBoolean("tintedpaper.fixes.target-place-shape-update", false,
            "Send shape updates after a powered target resets on placement; changes redstone behavior.");
        spongeAbsorbUpdate = globalConfig.getBoolean("tintedpaper.fixes.sponge-absorb-update", false,
            "Notify neighbors when an accepted absorption turns a sponge wet; changes block-update behavior.");
    }
}
