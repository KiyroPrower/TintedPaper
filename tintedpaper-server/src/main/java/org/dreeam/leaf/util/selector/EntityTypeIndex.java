package org.dreeam.leaf.util.selector;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

/**
 * Tracked entities of one level by entity type, so {@code @e[type=...]} does not scan every entity (ported from
 * DatapackOptimizer). An entity is in it from the moment the level starts tracking it until tracking ends, which is the set
 * the native selector scan visits. An entity type never changes while tracked. Buckets are bounded by the number of entity
 * types, so empty ones are kept.
 */
public final class EntityTypeIndex {
    private final Map<EntityType<?>, Set<Entity>> byType = new ConcurrentHashMap<>();

    public void add(final Entity entity) {
        this.byType.computeIfAbsent(entity.getType(), type -> ConcurrentHashMap.newKeySet()).add(entity);
    }

    public void remove(final Entity entity) {
        final Set<Entity> bucket = this.byType.get(entity.getType());
        if (bucket != null) {
            bucket.remove(entity);
        }
    }

    public Set<Entity> bucket(final EntityType<?> type) {
        final Set<Entity> bucket = this.byType.get(type);
        return bucket != null ? Collections.unmodifiableSet(bucket) : Collections.emptySet();
    }
}
