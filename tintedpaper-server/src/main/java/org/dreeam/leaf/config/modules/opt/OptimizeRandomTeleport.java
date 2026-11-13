package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class OptimizeRandomTeleport extends ConfigModule {
    public static volatile boolean enabled = true;

    public String basePath() {
        return ConfigCategory.PERF.basePath() + ".reduce-chunk-load-and-lookup";
    }

    @Override
    public void onLoaded() {
        enabled = globalConfig.getBoolean(basePath(), enabled);
    }
}
