package com.github.ezplugins.ezskills.ability;

import java.time.Instant;

/**
 * Tracks the lifecycle state of an ability for a single player.
 *
 * <p>An ability progresses through three states:
 * <ol>
 *   <li><b>Prepared</b> — the player has performed the activation gesture and the
 *       ability will trigger on the next eligible action within the preparation window.</li>
 *   <li><b>Active</b> — the ability is currently executing.</li>
 *   <li><b>Inactive</b> — neither prepared nor active.</li>
 * </ol>
 */
public final class AbilityState {

    /** The time this ability was prepared; {@code null} if not prepared. */
    private Instant preparedAt;

    /** Whether this ability is currently in the active state. */
    private boolean active;

    /** The expiry time of the active state; {@code null} if no time limit. */
    private Instant activeUntil;

    public AbilityState() { }

    /**
     * Returns whether this ability is in the prepared (charged) state and the
     * preparation window has not yet expired.
     *
     * @param windowMillis the preparation window length in milliseconds
     * @return {@code true} if still prepared
     */
    public boolean isPrepared(long windowMillis) {
        if (preparedAt == null) {
            return false;
        }
        return Instant.now().toEpochMilli() - preparedAt.toEpochMilli() < windowMillis;
    }

    /**
     * Returns whether the ability is currently active and its duration has not expired.
     *
     * @return {@code true} if active
     */
    public boolean isActive() {
        if (!active) {
            return false;
        }
        if (activeUntil != null && Instant.now().isAfter(activeUntil)) {
            active = false;
            return false;
        }
        return true;
    }

    public void setPreparedAt(Instant preparedAt) {
        this.preparedAt = preparedAt;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setActiveUntil(Instant activeUntil) {
        this.activeUntil = activeUntil;
    }

    /**
     * Convenience: sets active with a duration derived from the given ticks (20 ticks = 1 second).
     *
     * @param durationTicks duration in server ticks
     */
    public void setActiveDurationTicks(long durationTicks) {
        // 1 tick = 50 ms
        final long durationMillis = durationTicks * 50L;
        this.activeUntil = Instant.now().plusMillis(durationMillis);
        this.active = true;
    }

    public void clear() {
        this.preparedAt = null;
        this.active = false;
        this.activeUntil = null;
    }
}
