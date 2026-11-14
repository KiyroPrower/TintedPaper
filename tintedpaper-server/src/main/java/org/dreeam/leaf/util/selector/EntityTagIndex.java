package org.dreeam.leaf.util.selector;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;

/**
 * Entities of one level by scoreboard tag, so {@code @e[tag=x]} does not scan every entity (ported from DatapackOptimizer).
 *
 * <p>An entity is indexed from the moment the level starts tracking it until tracking ends (see
 * {@code ServerLevel.EntityCallbacks}), and its tag changes update the index while it is tracked. That is the set of
 * entities the native selector scan visits. A stale entry is never a wrong answer: callers evaluate the full selector
 * predicate on every candidate, so the index may only be too large, never too small.
 */
public final class EntityTagIndex {
    private final Map<String, Set<Entity>> tagToEntities = new ConcurrentHashMap<>();

    public void addEntityTag(final Entity entity, final String tag) {
        if (tag == null) {
            return;
        }
        this.tagToEntities.compute(tag, (key, set) -> {
            final Set<Entity> target = set != null ? set : ConcurrentHashMap.newKeySet();
            target.add(entity);
            return target;
        });
    }

    public void removeEntityTag(final Entity entity, final String tag) {
        if (tag == null) {
            return;
        }
        this.tagToEntities.computeIfPresent(tag, (key, set) -> {
            set.remove(entity);
            return set.isEmpty() ? null : set;
        });
    }

    public void indexEntity(final Entity entity) {
        for (final String tag : entity.entityTags()) {
            this.addEntityTag(entity, tag);
        }
    }

    public void removeEntity(final Entity entity) {
        for (final String tag : entity.entityTags()) {
            this.removeEntityTag(entity, tag);
        }
    }

    public Set<Entity> getEntitiesWithTag(final String tag) {
        if (tag == null) {
            return Collections.emptySet();
        }
        final Set<Entity> set = this.tagToEntities.get(tag);
        return set != null ? Collections.unmodifiableSet(set) : Collections.emptySet();
    }
}
