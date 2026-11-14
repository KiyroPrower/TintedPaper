package org.dreeam.leaf.config.modules.fixes;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class UpdateSuppression extends ConfigModule {
    public static volatile boolean crashFix = false;

    public String basePath() {
        return ConfigCategory.FIXES.basePath();
    }

    @Override
    public void onLoaded() {
        crashFix = globalConfig.getBoolean(basePath() + ".update-suppression-crash-fix", crashFix,
            "Stop update suppressors from crashing the server. A neighbor update that throws, or a shulker box comparator read " +
                "that fails with a ClassCastException, ends that update with a log line instead of a crash report. " +
                "Off keeps vanilla behavior.");
    }
}
