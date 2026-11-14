package org.dreeam.leaf.config.modules.network;

import org.dreeam.leaf.config.ConfigCategory;
import org.dreeam.leaf.config.ConfigModule;

public class SpectatorPackets extends ConfigModule {
    public static volatile boolean hideSpectatorGameMode = false;

    public String basePath() {
        return ConfigCategory.NETWORK.basePath();
    }

    @Override
    public void onLoaded() {
        hideSpectatorGameMode = globalConfig.getBoolean(basePath() + ".hide-spectator-game-mode", hideSpectatorGameMode,
            "Do not tell other players that someone is in spectator mode: the spectator is shown to them as survival in the player list. " +
                "The spectator still receives their own game mode. Off keeps vanilla behavior.");
    }
}
