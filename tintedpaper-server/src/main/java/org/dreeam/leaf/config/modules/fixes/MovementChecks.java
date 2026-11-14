package org.dreeam.leaf.config.modules.fixes;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class MovementChecks extends ConfigModule {
    public static volatile boolean alwaysAllowWeirdMovement = false;
    public static volatile boolean ignoreMovedTooQuicklyWhenLagging = false;

    public String basePath() {
        return ConfigCategory.FIXES.basePath() + ".movement";
    }

    @Override
    public void onLoaded() {
        alwaysAllowWeirdMovement = globalConfig.getBoolean(basePath() + ".always-allow-weird-movement", alwaysAllowWeirdMovement,
            "Allow speed/displacement failures by default; Paper events can still reject player movement. Collision, unloaded-chunk and invalid-packet checks remain enabled. Default false preserves Paper behavior.");
        ignoreMovedTooQuicklyWhenLagging = globalConfig.getBoolean(basePath() + ".ignore-moved-too-quickly-when-lagging", ignoreMovedTooQuicklyWhenLagging,
            "Allow the player speed check while the server reports lagging. Plugins can still reject the move.");
    }
}
