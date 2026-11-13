package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModule;

public final class DatapackOptimizations extends ConfigModule {
    public static volatile int nbtPathCacheSize = 1024;
    public static volatile boolean nbtListMatchLoops = true;
    public static volatile boolean nbtNamedReadPlan = true;
    public static volatile boolean unknownTagHolderSets = true;
    public static volatile boolean selectorAliveCheckLast = true;
    public static volatile boolean scoreboardTeamNullMember = true;
    public static volatile boolean scoreboardTrackedPlayersRace = true;
    public static volatile boolean movingBlocksDestroyPath = true;

    @Override
    public void onLoaded() {
        nbtPathCacheSize = Math.clamp(globalConfig.getInt("tintedpaper.datapacks.nbt-path-cache-size", 1024,
            "Maximum shared compiled NBT paths. Set to 0 to disable; bounded to 65536."), 0, 65536);
        nbtListMatchLoops = globalConfig.getBoolean("tintedpaper.datapacks.nbt-list-match-loops", true,
            "Avoid stream allocations while matching NBT list elements.");
        nbtNamedReadPlan = globalConfig.getBoolean("tintedpaper.datapacks.nbt-named-read-plan", true,
            "Reuse traversal scratch space for compound-only NBT paths; always read current tag values.");
        unknownTagHolderSets = globalConfig.getBoolean("tintedpaper.network.unknown-tag-holder-sets", true,
            "Encode undefined named holder sets directly instead of sending unknown tag names to clients.");
        selectorAliveCheckLast = globalConfig.getBoolean("tintedpaper.datapacks.selector-alive-check-last", true,
            "Evaluate selector option predicates before the implicit alive check.");
        scoreboardTeamNullMember = globalConfig.getBoolean("tintedpaper.fixes.scoreboard-team-null-member", true,
            "Omit invalid null team members when saving the scoreboard.");
        scoreboardTrackedPlayersRace = globalConfig.getBoolean("tintedpaper.fixes.scoreboard-tracked-players-race", true,
            "Collect tracked score holders without trusting a corrupted collection size counter.");
        movingBlocksDestroyPath = globalConfig.getBoolean("tintedpaper.fixes.moving-blocks-destroy-path", true,
            "Check the block carried by a piston above dirt paths instead of its moving placeholder.");
    }
}
