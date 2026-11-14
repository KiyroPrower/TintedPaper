package org.dreeam.leaf.util.selector;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.dreeam.leaf.config.modules.opt.DatapackOptimizations;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Serves {@code @e[type=X]} and {@code @e[type=#tag]} selectors without a box from {@link EntityTypeIndex}.
 *
 * <p>The first {@code verifySamples} unlimited answers are compared with the native scan (as sets of entities); on the first
 * difference the index is switched off until restart and the native answer is used. A selector with a limit or an order is
 * served only when at most one entity matches, since then no order or cut can change the answer.
 */
public final class TypeIndexScan {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** More member types than this and the native scan is cheaper than touching every bucket. */
    private static final int MAX_TAG_MEMBERS = 128;

    public static final AtomicBoolean DISABLED = new AtomicBoolean();
    public static final AtomicLong VERIFIED = new AtomicLong();
    public static final LongAdder HITS = new LongAdder();
    public static final LongAdder BAILS = new LongAdder();

    private TypeIndexScan() {
    }

    /** Matching entities from the index, or {@code null} when this selector must use the native scan. */
    public static @Nullable List<Entity> collect(final EntityTypeTest<Entity, ?> type, final @Nullable TagKey<EntityType<?>> typeTag, final ServerLevel level,
                                                 final Predicate<? super Entity> predicate, final boolean limited) {
        if (DISABLED.get()) {
            return null;
        }
        final List<Set<Entity>> buckets = new ArrayList<>(4);
        if (type instanceof EntityType<?> concrete) {
            buckets.add(level.typeIndex.bucket(concrete));
        } else if (typeTag != null) {
            final Optional<? extends HolderSet.Named<EntityType<?>>> members = BuiltInRegistries.ENTITY_TYPE.get(typeTag);
            if (members.isEmpty() || !members.get().isBound()) {
                BAILS.increment();
                return null;
            }
            for (final Holder<EntityType<?>> holder : members.get()) {
                if (buckets.size() == MAX_TAG_MEMBERS) {
                    BAILS.increment();
                    return null;
                }
                buckets.add(level.typeIndex.bucket(holder.value()));
            }
        } else {
            return null;
        }

        final List<Entity> matches = new ArrayList<>();
        for (final Set<Entity> bucket : buckets) {
            for (final Entity entity : bucket) {
                if (!accepts(level, type, predicate, entity)) {
                    continue;
                }
                matches.add(entity);
                if (limited && matches.size() > 1) {
                    // two or more matches: vanilla decides the order and the cut
                    BAILS.increment();
                    return null;
                }
            }
        }
        HITS.increment();
        return matches;
    }

    /** Whether this unlimited answer must still be compared with the native scan. */
    public static boolean shouldVerify() {
        return VERIFIED.get() < DatapackOptimizations.selectorIndexVerifySamples;
    }

    /** Compares an indexed answer with the native one; switches the index off on the first difference. */
    public static boolean verified(final List<Entity> indexed, final List<Entity> nativeResult) {
        if (indexed.size() == nativeResult.size() && sameEntities(indexed, nativeResult)) {
            VERIFIED.incrementAndGet();
            return true;
        }
        DISABLED.set(true);
        LOGGER.error("The entity type index returned {} entities where the native selector scan returned {}; the type index is now disabled until restart.",
            indexed.size(), nativeResult.size());
        return false;
    }

    static boolean sameEntities(final List<Entity> a, final List<Entity> b) {
        final Set<Entity> expected = Collections.newSetFromMap(new IdentityHashMap<>());
        expected.addAll(b);
        for (final Entity entity : a) {
            if (!expected.remove(entity)) {
                return false;
            }
        }
        return expected.isEmpty();
    }

    private static boolean accepts(final ServerLevel level, final EntityTypeTest<Entity, ?> type, final Predicate<? super Entity> predicate, final Entity entity) {
        return !entity.isRemoved() && level.getEntity(entity.getId()) == entity && type.tryCast(entity) != null && predicate.test(entity);
    }
}
