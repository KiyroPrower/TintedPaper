package org.dreeam.leaf.util.selector;

import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.dreeam.leaf.config.modules.opt.DatapackOptimizations;
import org.jspecify.annotations.Nullable;

public final class SelectorPredicateLookup {
    private final ResourceKey<LootItemCondition> key;
    private volatile @Nullable Snapshot current;

    public SelectorPredicateLookup(ResourceKey<LootItemCondition> key) {
        this.key = key;
    }

    public Optional<Holder.Reference<LootItemCondition>> get(HolderLookup.Provider provider) {
        if (!DatapackOptimizations.selectorPredicateLookup) {
            this.current = null;
            return provider.get(this.key);
        }
        Snapshot snapshot = this.current;
        if (snapshot == null || snapshot.provider != provider) {
            snapshot = new Snapshot(provider, provider.get(this.key));
            this.current = snapshot;
        }
        return snapshot.value;
    }

    // One immutable entry per selector avoids retaining arbitrary command keys in a global map.
    private record Snapshot(HolderLookup.Provider provider, Optional<Holder.Reference<LootItemCondition>> value) {}
}
