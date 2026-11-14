package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModule;

public final class SphericalExplosions extends ConfigModule {
    public static volatile boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = globalConfig.getBoolean("performance.explosions.spherical-rays", false,
            "Use Explodee spherical explosion rays. Changes destroyed blocks, neighboring TNT ignition and random consumption; false preserves native explosions.");
    }
}
