package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModule;

/** Potatoptimize ports (MIT, Tater-Certified/Potatoptimize). */
public final class PotatoptimizeCaches extends ConfigModule {
    public static volatile boolean slimeChunkCache = true;
    public static volatile boolean skipRiddenNavigation = false;
    public static volatile boolean skipPortalCollisionSearch = false;

    @Override
    public void onLoaded() {
        slimeChunkCache = globalConfig.getBoolean("performance.potatoptimize.slime-chunk-cache", true,
            "Cache the vanilla slime chunk roll in the chunk instead of reseeding a random for every slime spawn attempt. Identical results; Secure Seed keeps its own cache.");
        skipRiddenNavigation = globalConfig.getBoolean("performance.potatoptimize.skip-ridden-navigation", false,
            "Skip path navigation ticks for mobs riding a non-living vehicle such as a boat or minecart. Their path movement is not applied while riding, but path state is no longer advanced.");
        skipPortalCollisionSearch = globalConfig.getBoolean("performance.potatoptimize.skip-portal-collision-search", false,
            "Teleport through portals to the exact portal position without searching for a collision-free spot. Entities can arrive inside blocks; false preserves native behavior.");
    }
}
