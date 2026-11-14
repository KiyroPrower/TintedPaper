package org.dreeam.leaf.util.nbt;

import com.mojang.logging.LogUtils;
import net.minecraft.advancements.predicates.NbtPredicate;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.TagValueOutput;
import org.dreeam.leaf.config.modules.opt.NbtPartial;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Partial entity NBT serialization for read-only datapack paths (ported from DatapackOptimizer).
 *
 * <p>{@link #buildFilteredCompare} mirrors {@link NbtPredicate#getEntityTagToCompare} exactly (same problem reporter,
 * same {@code TagValueOutput}, same {@code saveWithoutId}, same player {@code SelectedItem} append); the only difference
 * is that the output goes through {@link KeyFilterValueOutput}, so keys the compare tag does not name are never encoded.
 * Matching still uses {@link NbtUtils#compareNbt}, which only reads the keys of the compare tag.
 */
public final class NbtPartialMatcher {
    private static final Logger LOGGER = LogUtils.getLogger();

    private NbtPartialMatcher() {
    }

    /**
     * @return the match result, or {@code null} to let the vanilla path run (feature off, self-disabled, empty compare tag or a failure)
     */
    public static @Nullable Boolean tryPartialMatch(final @Nullable Entity entity, final @Nullable CompoundTag compare) {
        if (!NbtPartial.active() || entity == null || compare == null || compare.isEmpty()) {
            return null;
        }
        try {
            final CompoundTag partial = buildFilteredCompare(entity, compare, compare.contains("SelectedItem"));
            final boolean partialResult = NbtUtils.compareNbt(compare, partial, true);

            if (NbtPartial.verifySamples > 0 && NbtPartial.verified.get() < NbtPartial.verifySamples) {
                final CompoundTag full = NbtPredicate.getEntityTagToCompare(entity);
                final boolean fullResult = NbtUtils.compareNbt(compare, full, true);
                if (partialResult != fullResult) {
                    NbtPartial.disabledByMismatch.set(true);
                    NbtPartial.fullFallbacks.increment();
                    LOGGER.error("Partial entity NBT differed from the full serialization (partial={}, full={}) for compare tag {}; "
                        + "partial NBT is now disabled until restart.", partialResult, fullResult, compare);
                    return fullResult;
                }
                NbtPartial.verified.incrementAndGet();
            }

            NbtPartial.partialHits.increment();
            return partialResult;
        } catch (Throwable failure) {
            // an optimization must never change an answer; run the vanilla path
            NbtPartial.fullFallbacks.increment();
            return null;
        }
    }

    /** The entity tag with only the keys {@code compare} names, built exactly like {@code getEntityTagToCompare}. */
    public static CompoundTag buildFilteredCompare(final Entity entity, final CompoundTag compare, final boolean wantsSelectedItem) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(entity.problemPath(), LOGGER)) {
            final TagValueOutput real = TagValueOutput.createWithContext(reporter, entity.registryAccess());
            entity.saveWithoutId(new KeyFilterValueOutput(real, compare));
            if (wantsSelectedItem && entity instanceof Player player) {
                final ItemStack selected = player.getInventory().getSelectedItem();
                if (!selected.isEmpty()) {
                    real.store("SelectedItem", ItemStack.CODEC, selected);
                }
            }
            return real.buildResult();
        }
    }

    /** Entity tag for {@code nbt} predicates: filtered when active, otherwise the full tag. */
    public static CompoundTag tagForPredicate(final Entity entity, final NbtPredicate nbt) {
        final CompoundTag compare = nbt.tag();
        if (!NbtPartial.active() || compare == null || compare.isEmpty()) {
            return NbtPredicate.getEntityTagToCompare(entity);
        }
        NbtPartial.entityPredicateFiltered.increment();
        return buildFilteredCompare(entity, compare, compare.contains("SelectedItem"));
    }
}
