/* BetterMaps idea by Nerjal, https://codeberg.org/NerjalNosk/bettermaps.
 * Copyright (c) 2023 Nerjal. MIT license, see licenses/BetterMaps-MIT.txt.
 * Native Paper adaptation retains owner-thread lookup and native map-fill events.
 */
package org.dreeam.leaf.util.maps;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.storage.loot.functions.ExplorationMapFunction;
import org.jspecify.annotations.Nullable;

public final class DeferredExplorerMaps {
    public static final String KEY = "tintedpaper:deferred_explorer_map";

    private DeferredExplorerMaps() {
    }

    public static @Nullable ItemStack defer(final ExplorationMapFunction function, final ItemStack original,
                                            final ServerLevel level, final BlockPos origin, final boolean trade) {
        if (!org.dreeam.leaf.config.modules.opt.DeferredExplorerMaps.enabled || !function.tintedCanDefer()
            || (!original.is(Items.MAP) && !original.is(Items.FILLED_MAP)) || !level.getWorldBorder().isWithinBounds(origin)) {
            return null;
        }
        Tag encoded = ExplorationMapFunction.MAP_CODEC.codec()
            .encodeStart(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), function).result().orElse(null);
        if (!(encoded instanceof CompoundTag encodedFunction)) {
            return null;
        }
        encodedFunction.remove("condition"); // Already evaluated by the loot pipeline; never reevaluate it on map use.
        CompoundTag request = new CompoundTag();
        request.put("function", encoded);
        request.putString("dimension", level.dimension().identifier().toString());
        request.putIntArray("origin", new int[]{origin.getX(), origin.getY(), origin.getZ()});
        request.putBoolean("trade", trade);
        ItemStack token = original.transmuteCopy(Items.MAP);
        token.remove(DataComponents.MAP_ID);
        CustomData.update(DataComponents.CUSTOM_DATA, token, data -> data.put(KEY, request));
        return token;
    }

    /** Null means a normal map; EMPTY means an unresolved token, which must not be consumed. */
    public static @Nullable ItemStack resolve(final ServerLevel level, final ItemStack token) {
        CustomData data = token.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(KEY)) {
            return null;
        }
        CompoundTag request = data.copyTag().getCompound(KEY).orElse(null);
        if (request == null || !level.dimension().identifier().toString().equals(request.getStringOr("dimension", ""))) {
            return ItemStack.EMPTY;
        }
        int[] coordinates = request.getIntArray("origin").orElse(null);
        Tag encoded = request.get("function");
        if (coordinates == null || coordinates.length != 3 || !(encoded instanceof CompoundTag encodedFunction)
            || encodedFunction.contains("condition") || !encodedFunction.getStringOr("destination", "").startsWith("#")) {
            return ItemStack.EMPTY;
        }
        BlockPos origin = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);
        if (!level.getWorldBorder().isWithinBounds(origin)) {
            return ItemStack.EMPTY;
        }
        ExplorationMapFunction function = ExplorationMapFunction.MAP_CODEC.codec()
            .parse(level.registryAccess().createSerializationContext(NbtOps.INSTANCE), encoded).result().orElse(null);
        if (function == null || !function.tintedCanDefer()) {
            return ItemStack.EMPTY;
        }
        ItemStack map = token.transmuteCopy(Items.FILLED_MAP, 1);
        map.remove(DataComponents.MAP_ID);
        CustomData.update(DataComponents.CUSTOM_DATA, map, tag -> tag.remove(KEY));
        map = function.tintedCreateMap(map, level, origin, request.getBooleanOr("trade", false));
        return map.has(DataComponents.MAP_ID) ? map : ItemStack.EMPTY;
    }
}
