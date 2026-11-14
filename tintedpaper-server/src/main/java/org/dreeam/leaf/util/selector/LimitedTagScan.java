package org.dreeam.leaf.util.selector;

import java.util.List;
import java.util.function.Predicate;

/**
 * A selector with {@code limit=}, {@code sort=} or {@code @n} used to leave the tag index alone, because its result
 * depends on the order vanilla walks the entities in, which the index cannot reproduce. That order only matters when
 * more than one entity matches. With zero or one match every ordering and every truncation gives the same answer, and
 * that is the usual shape of a datapack state machine ({@code @e[tag=target_arrow,limit=1]},
 * {@code @e[tag=arrow_bait,limit=1,sort=nearest]}): vanilla walks the whole level to find out that nothing matches.
 *
 * <p>{@link #atMostOne} walks the (small) candidate bucket and gives up as soon as a second entity is accepted, so the
 * caller hands the selector to vanilla unchanged. The cost of giving up is bounded by that bucket.
 */
public final class LimitedTagScan {

    /**
     * Appends the single accepted candidate to {@code out}, or nothing when none is accepted, and returns true. Returns
     * false, with {@code out} untouched, when a second candidate is accepted: the answer then depends on order.
     */
    public static <E> boolean atMostOne(Iterable<? extends E> candidates, Predicate<? super E> accept, List<E> out) {
        E found = null;
        for (E candidate : candidates) {
            if (!accept.test(candidate)) {
                continue;
            }
            if (found != null) {
                return false;
            }
            found = candidate;
        }
        if (found != null) {
            out.add(found);
        }
        return true;
    }

    private LimitedTagScan() {}
}
