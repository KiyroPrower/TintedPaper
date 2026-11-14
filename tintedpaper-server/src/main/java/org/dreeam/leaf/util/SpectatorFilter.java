package org.dreeam.leaf.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameType;
import org.dreeam.leaf.config.modules.network.SpectatorPackets;

/** Player-list packets that do not tell other players somebody is a spectator; see {@link SpectatorPackets}. */
public final class SpectatorFilter {
    private SpectatorFilter() {
    }

    /** The same packet with every spectator entry shown as survival, or {@code packet} itself when nothing changes. */
    public static ClientboundPlayerInfoUpdatePacket hide(ClientboundPlayerInfoUpdatePacket packet) {
        if (!SpectatorPackets.hideSpectatorGameMode) {
            return packet;
        }
        boolean any = false;
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            if (entry.gameMode() == GameType.SPECTATOR) {
                any = true;
                break;
            }
        }
        if (!any) {
            return packet;
        }
        List<ClientboundPlayerInfoUpdatePacket.Entry> entries = new ArrayList<>(packet.entries().size());
        for (ClientboundPlayerInfoUpdatePacket.Entry entry : packet.entries()) {
            entries.add(entry.gameMode() != GameType.SPECTATOR ? entry : new ClientboundPlayerInfoUpdatePacket.Entry(
                entry.profileId(), entry.profile(), entry.listed(), entry.latency(), GameType.SURVIVAL,
                entry.displayName(), entry.showHat(), entry.listOrder(), entry.chatSession()));
        }
        return new ClientboundPlayerInfoUpdatePacket(packet.actions(), entries);
    }

    /** Game mode change of {@code changed}: it gets the real packet, everyone who can see it gets the filtered one. */
    public static void broadcastGameMode(PlayerList playerList, ServerPlayer changed, ClientboundPlayerInfoUpdatePacket packet) {
        ClientboundPlayerInfoUpdatePacket others = hide(packet);
        for (ServerPlayer player : playerList.getPlayers()) {
            if (player != changed && !player.getBukkitEntity().canSee(changed.getBukkitEntity())) {
                continue;
            }
            player.connection.send(player == changed ? packet : others);
        }
    }
}
