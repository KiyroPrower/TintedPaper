package org.dreeam.leaf.util.selector;

import io.papermc.paper.util.SizeLimitedSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Predicate;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/** Keeps the live Bukkit tag set and its tracking index together, including bulk and iterator mutations. */
public final class IndexedEntityTags extends SizeLimitedSet<String> {
    private final Entity entity;
    private @Nullable EntityTagIndex index;

    public IndexedEntityTags(final Entity entity, final int limit) {
        super(new CopyOnWriteArraySet<>(), limit);
        this.entity = entity;
    }

    public synchronized void track(final @Nullable EntityTagIndex index) {
        if (this.index != null) {
            this.index.removeEntity(this.entity);
        }
        this.index = index;
        if (index != null) {
            index.indexEntity(this.entity);
        }
    }

    @Override
    public synchronized boolean add(final String tag) {
        if (!super.add(tag)) {
            return false;
        }
        if (this.index != null) {
            this.index.addEntityTag(this.entity, tag);
        }
        return true;
    }

    @Override
    public synchronized boolean addAll(final Collection<? extends String> tags) {
        final ArrayList<String> snapshot = new ArrayList<>(tags);
        if (!super.addAll(snapshot)) {
            return false;
        }
        if (this.index != null) {
            for (final String tag : snapshot) {
                this.index.addEntityTag(this.entity, tag);
            }
        }
        return true;
    }

    @Override
    public synchronized boolean remove(final @Nullable Object tag) {
        if (!super.remove(tag)) {
            return false;
        }
        if (this.index != null && tag instanceof String string) {
            this.index.removeEntityTag(this.entity, string);
        }
        return true;
    }

    @Override
    public synchronized boolean removeAll(final Collection<?> tags) {
        Objects.requireNonNull(tags);
        return this.removeIf(tags::contains);
    }

    @Override
    public synchronized boolean retainAll(final Collection<?> tags) {
        Objects.requireNonNull(tags);
        return this.removeIf(tag -> !tags.contains(tag));
    }

    @Override
    public synchronized boolean removeIf(final Predicate<? super String> predicate) {
        Objects.requireNonNull(predicate);
        boolean changed = false;
        for (final String tag : this) {
            if (predicate.test(tag)) {
                changed |= this.remove(tag);
            }
        }
        return changed;
    }

    @Override
    public synchronized void clear() {
        if (this.index != null) {
            this.index.removeEntity(this.entity);
        }
        super.clear();
    }
}
