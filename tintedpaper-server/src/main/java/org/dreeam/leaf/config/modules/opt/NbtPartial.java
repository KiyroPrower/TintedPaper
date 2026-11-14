package org.dreeam.leaf.config.modules.opt;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import org.dreeam.leaf.config.ConfigModule;

public class NbtPartial extends ConfigModule {
    public static volatile boolean enabled = true;
    public static volatile int verifySamples = 500;
    public static volatile boolean selectorReadFilter = true;

    /** Set when a partial result differed from the full serialization; partial matching then stays off until restart. */
    public static final AtomicBoolean disabledByMismatch = new AtomicBoolean();
    public static final AtomicLong verified = new AtomicLong();
    public static final LongAdder partialHits = new LongAdder();
    public static final LongAdder fullFallbacks = new LongAdder();
    public static final LongAdder selectorFiltered = new LongAdder();
    public static final LongAdder selectorItemSkips = new LongAdder();
    public static final LongAdder entityPredicateFiltered = new LongAdder();

    public String basePath() {
        return "tintedpaper.datapacks.nbt-partial";
    }

    @Override
    public void onLoaded() {
        enabled = globalConfig.getBoolean(basePath() + ".enabled", enabled,
            "Serialize only the entity NBT keys a datapack reads (@e[nbt=], entity predicates and NbtPredicate) instead of the whole entity. " +
                "The result is identical; only the work for keys nobody reads is skipped.");
        verifySamples = Math.max(0, globalConfig.getInt(basePath() + ".verify-samples", verifySamples,
            "Compare this many partial results against the full serialization at startup. On the first difference partial matching turns itself off. 0 disables the check."));
        selectorReadFilter = globalConfig.getBoolean(basePath() + ".selector-read-filter", selectorReadFilter,
            "In @e[nbt=], skip the player's SelectedItem when the compared tag does not mention it.");
    }

    public static boolean active() {
        return enabled && !disabledByMismatch.get();
    }
}
