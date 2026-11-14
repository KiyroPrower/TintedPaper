package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class SpawnTrackingRange extends ConfigModule {
    public static volatile int blocks = 128;

    public String basePath() {
        return ConfigCategory.PERF.basePath() + ".chunks";
    }

    @Override
    public void onLoaded() {
        blocks = Math.clamp(globalConfig.getInt(basePath() + ".player-near-chunk-detection-range", blocks,
            "Player detection radius in blocks (0-1024; default 128). Zero disables detection. Paper spawn-event radii remain upper bounds. Tracking maps refresh on the next player tracking update; larger radii track more chunks."), 0, 1024);
    }

    public static int chunks() {
        return (blocks + 8) / 16;
    }

    public static double squared() {
        int range = blocks;
        return (double) range * range;
    }

    public static double capSquared(double paperRangeSquared) {
        int range = blocks;
        // Preserve existing plugin-expanded Paper event radii at the default setting.
        return range == 128 ? paperRangeSquared : Math.min(paperRangeSquared, (double) range * range);
    }
}
