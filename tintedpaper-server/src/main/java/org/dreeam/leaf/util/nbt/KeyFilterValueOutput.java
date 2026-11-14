package org.dreeam.leaf.util.nbt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import java.util.Set;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * A {@link ValueOutput} that delegates to a real {@code TagValueOutput} but only
 * for a chosen set of top-level keys — skipping the encode entirely for every
 * other key (PR #204, extended in PR #221).
 *
 * <h2>Why this is correctness-safe (no GOTCHA #10)</h2>
 *
 * We never reproduce any NBT ourselves. For a kept key we delegate to the real
 * {@code TagValueOutput}, so vanilla performs the exact same encode → the
 * resulting subtree is byte-for-byte identical to a full serialization. For a
 * dropped key we route the write into {@link NoOpValueOutput}, so its
 * (expensive) codec encode never runs. Every consumer only reads keys it asked
 * for, all of which are kept and vanilla-encoded — so the observable result is
 * identical to vanilla, while the heavy unused field codecs never execute.
 *
 * <h2>What gets filtered (and the PR #221 fix)</h2>
 *
 * PR #204 filtered only the <em>keyed value writes</em>
 * ({@code store}/{@code storeNullable}/{@code putX}) and delegated the
 * <em>structural</em> writes ({@code child}/{@code childrenList}/{@code list})
 * unchanged, on the assumption that those were cheap or empty. A {@code javap}
 * sweep of the 26.2 save path disproved that assumption:
 *
 * <pre>
 * Player.addAdditionalSaveData:       list("Inventory",  ItemStack.CODEC)   &lt;-- heaviest key
 *                                     list("EnderItems", ItemStack.CODEC)
 * Entity.saveWithoutId:               childrenList("Passengers")
 * ServerPlayer.saveParentVehicle:     child("RootVehicle")
 * ServerPlayer.saveEnderPearls:       childrenList("ender_pearls")
 * </pre>
 *
 * The two {@code list} calls are where a player's item {@code DataComponents} are
 * encoded — i.e. the single most expensive part of serializing a player was
 * being delegated, never skipped. All three structural kinds are now filtered
 * too. There are <strong>zero</strong> keyless {@code store(MapCodec, …)}
 * callsites anywhere on the {@code Entity}/{@code LivingEntity}/{@code Player}/
 * {@code ServerPlayer} save path, so delegating that one unchanged costs nothing.
 *
 * <h2>Scope of the saving</h2>
 *
 * Filtering skips the <em>encode</em>, not the evaluation of the argument being
 * passed: {@code store("attributes", CODEC, this.getAttributes().pack())} still
 * runs {@code pack()}, but never {@code CODEC.encodeStart}. Separate native
 * preparation hooks avoid discarded inventory records, recipe copies, effects
 * lists and Brain snapshots. Only native compare tags and owned single-key
 * roots expose discard membership to those hooks; arbitrary predicates and
 * caller-owned Sets retain their original evaluation semantics.
 *
 * <h2>Self-disabling</h2>
 *
 * Both entry points ({@code NbtPartialMatcher}) cross-check the first N results
 * against vanilla and permanently disable the feature on any divergence.
 */
public final class KeyFilterValueOutput implements ValueOutput {

    /** Dropped-item {@code Item} compounds written as just their id instead of the full stack encoding. */
    public static final LongAdder ITEM_ID_ONLY = new LongAdder();

    private final ValueOutput delegate;
    private final Predicate<String> wanted;
    private final CompoundTag compareTag;
    private final String rootKey;

    /**
     * Keeps the top-level keys present in a predicate's compare tag — the
     * {@code @e[nbt={…}]} path.
     */
    public KeyFilterValueOutput(ValueOutput delegate, CompoundTag compareTag) {
        this(delegate, compareTag::contains, compareTag, null);
    }

    /** Keeps a fixed key set — the {@code /data get … <path>} path. */
    public KeyFilterValueOutput(ValueOutput delegate, Set<String> keys) {
        this(delegate, keys::contains);
    }

    public KeyFilterValueOutput(ValueOutput delegate, Predicate<String> wanted) {
        this(delegate, wanted, null, null);
    }

    private KeyFilterValueOutput(ValueOutput delegate, Predicate<String> wanted,
                                CompoundTag compareTag, String rootKey) {
        this.delegate = delegate;
        this.wanted = wanted;
        this.compareTag = compareTag;
        this.rootKey = rootKey;
    }

    /** A single native path root; other constructors retain their original live predicates. */
    public static KeyFilterValueOutput forRootKey(ValueOutput delegate, String key) {
        return new KeyFilterValueOutput(delegate, key::equals, null, key);
    }

    /** Never probes arbitrary predicates or caller-owned Sets, and never snapshots a mutable tag. */
    public boolean discardsPreparation(String key) {
        return this.compareTag != null ? !this.compareTag.contains(key)
                : this.rootKey != null && !this.rootKey.equals(key);
    }

    private boolean keep(String key) {
        return this.wanted.test(key);
    }

    // ── keyed value writes: FILTERED ──

    @Override
    public <T> void store(String key, Codec<T> codec, T value) {
        if (!keep(key)) return;
        // ItemEntity.addAdditionalSaveData stores the stack as "Item" with ItemStack.CODEC (javap, build 18).
        // A compare of exactly Item:{id:"..."} observes only the id, which the codec writes as the registry
        // key string; the count and the component patch are the expensive part and cannot change the result.
        if (this.compareTag != null && value instanceof ItemStack stack && codec == ItemStack.CODEC
                && "Item".equals(key) && !stack.isEmpty()
                && this.compareTag.get("Item") instanceof CompoundTag wantedItem && wantedItem.size() == 1
                && wantedItem.get("id") instanceof StringTag) {
            this.delegate.child("Item").putString("id", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
            ITEM_ID_ONLY.increment();
            return;
        }
        this.delegate.store(key, codec, value);
    }

    @Override
    public <T> void storeNullable(String key, Codec<T> codec, T value) {
        if (keep(key)) this.delegate.storeNullable(key, codec, value);
    }

    @Override
    public void putBoolean(String key, boolean value) {
        if (keep(key)) this.delegate.putBoolean(key, value);
    }

    @Override
    public void putByte(String key, byte value) {
        if (keep(key)) this.delegate.putByte(key, value);
    }

    @Override
    public void putShort(String key, short value) {
        if (keep(key)) this.delegate.putShort(key, value);
    }

    @Override
    public void putInt(String key, int value) {
        if (keep(key)) this.delegate.putInt(key, value);
    }

    @Override
    public void putLong(String key, long value) {
        if (keep(key)) this.delegate.putLong(key, value);
    }

    @Override
    public void putFloat(String key, float value) {
        if (keep(key)) this.delegate.putFloat(key, value);
    }

    @Override
    public void putDouble(String key, double value) {
        if (keep(key)) this.delegate.putDouble(key, value);
    }

    @Override
    public void putString(String key, String value) {
        if (keep(key)) this.delegate.putString(key, value);
    }

    @Override
    public void putIntArray(String key, int[] value) {
        if (keep(key)) this.delegate.putIntArray(key, value);
    }

    // ── structural writes: FILTERED (PR #221 — this is where Inventory lived) ──

    @Override
    public ValueOutput child(String key) {
        return keep(key) ? this.delegate.child(key) : NoOpValueOutput.INSTANCE;
    }

    @Override
    public ValueOutput.ValueOutputList childrenList(String key) {
        return keep(key) ? this.delegate.childrenList(key) : NoOpValueOutput.EMPTY_CHILDREN;
    }

    @Override
    public <T> ValueOutput.TypedOutputList<T> list(String key, Codec<T> codec) {
        return keep(key) ? this.delegate.list(key, codec) : NoOpValueOutput.emptyTypedList();
    }

    // ── keyless / bookkeeping: DELEGATED unchanged ──

    @Override
    public <T> void store(MapCodec<T> codec, T value) {
        // No key to filter on. Verified by javap: zero callsites on the entity
        // save path, so delegating is free.
        this.delegate.store(codec, value);
    }

    @Override
    public void discard(String key) {
        // Delegated unconditionally: discarding a key we never wrote is a no-op,
        // and for a kept key vanilla's intent to remove it must be honoured.
        this.delegate.discard(key);
    }

    @Override
    public boolean isEmpty() {
        return this.delegate.isEmpty();
    }
}
