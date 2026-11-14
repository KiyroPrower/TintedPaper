package org.dreeam.leaf.util.selector;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityTypeTest;

/** Serves {@code @e[tag=...]} selectors from {@link EntityTagIndex}; see {@code EntitySelector.addEntities}. */
public final class TagIndexScan {
    public static final LongAdder HITS = new LongAdder();
    public static final LongAdder LIMIT_SERVED = new LongAdder();
    public static final LongAdder BAIL_LIMIT = new LongAdder();

    private TagIndexScan() {
    }

    /**
     * @param limited the selector has a result limit or an order, so the native scan may stop early or the caller sorts
     * @return {@code true} when {@code result} was filled and the native scan must be skipped
     */
    public static boolean serve(final List<String> positiveTags, final EntityTypeTest<Entity, ?> type, final ServerLevel level,
                                final Predicate<? super Entity> predicate, final List<Entity> result, final boolean limited) {
        final Set<Entity> candidates = smallestBucket(positiveTags, level.tagIndex);
        if (candidates == null) {
            return false;
        }
        if (limited) {
            // With zero or one match no order or truncation can change the answer; with two or more vanilla decides.
            if (!result.isEmpty()) {
                BAIL_LIMIT.increment();
                return false;
            }
            final List<Entity> single = new ArrayList<>(1);
            // Predicates can consume random state; never evaluate them before deciding to fall back.
            if (!LimitedTagScan.atMostOne(candidates, entity -> true, single)) {
                BAIL_LIMIT.increment();
                return false;
            }
            if (!single.isEmpty() && accepts(level, type, predicate, single.getFirst())) {
                result.add(single.getFirst());
            }
            LIMIT_SERVED.increment();
            HITS.increment();
            return true;
        }
        for (final Entity entity : candidates) {
            if (accepts(level, type, predicate, entity)) {
                result.add(entity);
            }
        }
        HITS.increment();
        return true;
    }

    private static Set<Entity> smallestBucket(final List<String> tags, final EntityTagIndex index) {
        Set<Entity> smallest = null;
        for (int i = 0, n = tags.size(); i < n; i++) {
            final Set<Entity> bucket = index.getEntitiesWithTag(tags.get(i));
            if (bucket.isEmpty()) {
                return bucket;
            }
            if (smallest == null || bucket.size() < smallest.size()) {
                smallest = bucket;
            }
        }
        return smallest;
    }

    private static boolean accepts(final ServerLevel level, final EntityTypeTest<Entity, ?> type, final Predicate<? super Entity> predicate, final Entity entity) {
        return !entity.isRemoved() && level.getEntity(entity.getId()) == entity && type.tryCast(entity) != null && predicate.test(entity);
    }
}
