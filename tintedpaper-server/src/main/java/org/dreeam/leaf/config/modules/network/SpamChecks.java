package org.dreeam.leaf.config.modules.network;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class SpamChecks extends ConfigModule {
    public static volatile boolean disableDisconnectSpam = false;

    public String basePath() {
        return ConfigCategory.NETWORK.basePath();
    }

    @Override
    public void onLoaded() {
        disableDisconnectSpam = globalConfig.getBoolean(basePath() + ".disable-disconnect-spam", disableDisconnectSpam,
            "Suppress spam disconnects. Rate-limited recipe and suggestion requests are still discarded; malformed tag suggestions are still rejected.");
    }
}
