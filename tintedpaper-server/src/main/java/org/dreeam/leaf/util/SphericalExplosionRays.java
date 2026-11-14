/* Adapted from Explodee by KnownSH, https://github.com/KnownSH/Explodee.
 * Copyright 2025 KnownSH. SPDX-License-Identifier: MIT-0
 * See licenses/Explodee-MIT-0.txt for the original license.
 */
package org.dreeam.leaf.util;

public final class SphericalExplosionRays {
    private static final double ANGLE_STEP = Math.PI * (1 + Math.sqrt(5));
    private static final double[] COMMON_RAYS = new double[300];

    static {
        for (int ray = 0; ray < 100; ray++) {
            calculate(ray, 100, COMMON_RAYS, ray * 3);
        }
    }

    private SphericalExplosionRays() {
    }

    public static int count(final float radius) {
        return (int) Math.max(radius * radius, 100);
    }

    public static void direction(final int ray, final int count, final double[] target) {
        if (count == 100) {
            System.arraycopy(COMMON_RAYS, ray * 3, target, 0, 3);
        } else {
            calculate(ray, count, target, 0);
        }
    }

    private static void calculate(final int ray, final int count, final double[] target, final int offset) {
        final double phi = Math.acos(1 - 2.0 * ray / count);
        final double theta = ANGLE_STEP * ray;
        final double sinPhi = Math.sin(phi);
        target[offset] = sinPhi * Math.cos(theta);
        target[offset + 1] = sinPhi * Math.sin(theta);
        target[offset + 2] = Math.cos(phi);
    }
}
