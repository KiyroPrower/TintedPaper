package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModule;

public final class DeferredExplorerMaps extends ConfigModule {
    public static volatile boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = globalConfig.getBoolean("performance.maps.defer-explorer-map-search", false,
            "Defer eligible loot and cartographer map searches until the map is used. Searches remain on the world thread and can pause that tick; existing deferred maps still resolve when disabled.");
    }
}
