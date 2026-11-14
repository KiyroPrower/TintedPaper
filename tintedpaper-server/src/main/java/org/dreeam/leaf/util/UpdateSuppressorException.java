package org.dreeam.leaf.util;

import net.minecraft.core.BlockPos;

/**
 * Thrown in place of a crash when an update suppressor is detected. It carries no stack trace: it only unwinds the
 * update that triggered it and is logged by whoever catches it.
 */
public final class UpdateSuppressorException extends RuntimeException {
    public UpdateSuppressorException(BlockPos pos, Object source) {
        super("Update suppressed at " + pos.toShortString() + " by " + source, null, false, false);
    }
}
