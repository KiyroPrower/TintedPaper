package org.dreeam.leaf.config.modules.gameplay;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class BlockMechanics extends ConfigModule {
    public static volatile boolean copperBulbOneTickDelay = false;
    public static volatile boolean crafterOneTickDelay = false;
    public static volatile boolean disableLeafDecay = false;

    public String basePath() {
        return ConfigCategory.GAMEPLAY.basePath() + ".blocks";
    }

    @Override
    public void onLoaded() {
        copperBulbOneTickDelay = globalConfig.getBoolean(basePath() + ".copper-bulb-one-tick-delay", copperBulbOneTickDelay);
        crafterOneTickDelay = globalConfig.getBoolean(basePath() + ".crafter-one-tick-delay", crafterOneTickDelay);
        disableLeafDecay = globalConfig.getBoolean(basePath() + ".disable-leaf-decay", disableLeafDecay);
    }
}
