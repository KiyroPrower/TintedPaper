package org.dreeam.leaf.util.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A {@link ValueOutput} that discards everything written to it, plus matching
 * no-op implementations of the two nested list interfaces.
 *
 * <p>Used by {@link KeyFilterValueOutput} for the <em>structural</em> writes
 * ({@code child} / {@code childrenList} / {@code list}) of keys that are not
 * wanted. Returning a no-op sink instead of delegating is what actually skips
 * the expensive work: for {@code ValueOutput.list("Inventory", ItemStack.CODEC)}
 * the per-item {@code Codec} encode happens inside
 * {@link ValueOutput.TypedOutputList#add(Object)}, so a no-op {@code add} means
 * the whole inventory (every item's {@code DataComponents}) is never encoded.
 *
 * <h2>Why {@code isEmpty()} must report {@code true}</h2>
 *
 * Vanilla reads back from these outputs in exactly one shape — "if nothing was
 * written, drop the key":
 *
 * <pre>
 * // Entity.saveWithoutId (bytecode-verified, 26.2)
 * ValueOutput.ValueOutputList list = out.childrenList("Passengers");
 * for (Entity passenger : this.getPassengers()) { … }
 * if (list.isEmpty()) out.discard("Passengers");
 * </pre>
 *
 * Reporting {@code true} makes vanilla call {@code discard("Passengers")} on the
 * <em>real</em> output, which is a no-op because we never wrote that key there.
 * Reporting {@code false} would instead leave vanilla believing it had written a
 * key that does not exist. The other three structural sites on the player save
 * path ({@code Inventory}, {@code EnderItems} via {@code list};
 * {@code RootVehicle} via {@code child}; {@code ender_pearls} via
 * {@code childrenList}) never read back at all.
 */
public final class NoOpValueOutput implements ValueOutput {

    /** Shared instance — stateless, so a singleton is safe across threads. */
    public static final NoOpValueOutput INSTANCE = new NoOpValueOutput();

    /** Shared no-op {@link ValueOutput.ValueOutputList}. */
    public static final ValueOutput.ValueOutputList EMPTY_CHILDREN = new NoOpChildrenList();

    private static final TypedList<?> EMPTY_TYPED = new TypedList<>();

    private NoOpValueOutput() {}

    /**
     * Shared no-op {@link ValueOutput.TypedOutputList}. The element type is
     * irrelevant because {@code add} discards, so one raw instance serves all
     * codecs.
     */
    @SuppressWarnings("unchecked")
    public static <T> ValueOutput.TypedOutputList<T> emptyTypedList() {
        return (ValueOutput.TypedOutputList<T>) EMPTY_TYPED;
    }

    // ── every write is discarded ──

    @Override
    public <T> void store(String key, Codec<T> codec, T value) {}

    @Override
    public <T> void storeNullable(String key, Codec<T> codec, T value) {}

    @Override
    public <T> void store(MapCodec<T> codec, T value) {}

    @Override
    public void putBoolean(String key, boolean value) {}

    @Override
    public void putByte(String key, byte value) {}

    @Override
    public void putShort(String key, short value) {}

    @Override
    public void putInt(String key, int value) {}

    @Override
    public void putLong(String key, long value) {}

    @Override
    public void putFloat(String key, float value) {}

    @Override
    public void putDouble(String key, double value) {}

    @Override
    public void putString(String key, String value) {}

    @Override
    public void putIntArray(String key, int[] value) {}

    @Override
    public void discard(String key) {}

    /** Nested outputs are also discarding, so a whole subtree costs nothing. */
    @Override
    public ValueOutput child(String key) {
        return this;
    }

    @Override
    public ValueOutput.ValueOutputList childrenList(String key) {
        return EMPTY_CHILDREN;
    }

    @Override
    public <T> ValueOutput.TypedOutputList<T> list(String key, Codec<T> codec) {
        return emptyTypedList();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    private static final class NoOpChildrenList implements ValueOutput.ValueOutputList {

        @Override
        public ValueOutput addChild() {
            return INSTANCE;
        }

        @Override
        public void discardLast() {}

        @Override
        public boolean isEmpty() {
            return true;
        }
    }

    private static final class TypedList<T> implements ValueOutput.TypedOutputList<T> {

        /** The skipped {@code Codec} encode lives here — this is the actual saving. */
        @Override
        public void add(T value) {}

        @Override
        public boolean isEmpty() {
            return true;
        }
    }
}
