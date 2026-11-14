package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class NbtWriteCache extends ConfigModule {
    public static volatile int softLimit = 8192;
    public static volatile int hardLimit = 32678;

    public String basePath() {
        return ConfigCategory.PERF.basePath() + ".chunks";
    }

    @Override
    public void onLoaded() {
        softLimit = Math.max(1, globalConfig.getInt(basePath() + ".chunk-data-cache-soft-limit", softLimit));
        hardLimit = Math.max(softLimit, globalConfig.getInt(basePath() + ".chunk-data-cache-limit", hardLimit));
    }
}
