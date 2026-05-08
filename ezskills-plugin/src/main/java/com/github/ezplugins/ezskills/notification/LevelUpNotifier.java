package com.github.ezplugins.ezskills.notification;

import com.github.ezplugins.ezskills.api.event.SkillLevelUpEvent;
import com.github.ezplugins.ezskills.config.ConfigManager;
import com.skyblockexp.ezcountdown.api.EzCountdownApi;
import com.skyblockexp.ezcountdown.api.event.CountdownEndEvent;
import com.skyblockexp.ezcountdown.api.model.Countdown;
import com.skyblockexp.ezcountdown.api.model.CountdownBuilder;
import com.skyblockexp.ezcountdown.api.model.CountdownType;
import com.skyblockexp.ezcountdown.display.DisplayType;
import java.time.Duration;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Bridges EzSkills level-up events to EzCountdown boss-bar notifications.
 *
 * <p>When a player levels up and {@code notifications.bossbar.enabled} is {@code true},
 * a short-lived {@link Countdown} is created via the EzCountdown API and displayed
 * as a boss bar to all online players. The countdown is automatically deleted
 * once it expires.</p>
 *
 * <p>This listener is only registered when EzCountdown is installed on the server.</p>
 */
public final class LevelUpNotifier implements Listener {

    /** Prefix for all countdown IDs managed by this class. */
    private static final String ID_PREFIX = "ezskills_lu_";

    /** EzCountdown service API obtained from the Bukkit services manager. */
    private final EzCountdownApi countdownApi;

    /** Provides access to main configuration for notification settings. */
    private final ConfigManager configManager;

    private LevelUpNotifier(@NotNull EzCountdownApi countdownApi,
                            @NotNull ConfigManager configManager) {
        this.countdownApi = countdownApi;
        this.configManager = configManager;
    }

    /**
     * Attempts to obtain the EzCountdown service and construct a {@link LevelUpNotifier}.
     *
     * @param configManager the config manager used for notification settings
     * @return a new notifier, or {@code null} if EzCountdown is not available
     */
    @Nullable
    public static LevelUpNotifier tryCreate(@NotNull ConfigManager configManager) {
        final RegisteredServiceProvider<EzCountdownApi> rsp =
                Bukkit.getServicesManager().getRegistration(EzCountdownApi.class);
        if (rsp == null) {
            return null;
        }
        return new LevelUpNotifier(rsp.getProvider(), configManager);
    }

    /**
     * Creates a boss-bar countdown via EzCountdown when a player levels up.
     *
     * <p>Any existing countdown for the same player is stopped and replaced so
     * rapid consecutive level-ups remain visible.</p>
     *
     * @param event the level-up event
     */
    @EventHandler
    public void onLevelUp(@NotNull SkillLevelUpEvent event) {
        if (!configManager.getMainConfig().getBoolean("notifications.bossbar.enabled", true)) {
            return;
        }

        final int durationSeconds =
                configManager.getMainConfig().getInt("notifications.bossbar.duration", 5);
        final String countdownId =
                ID_PREFIX + event.getPlayer().getUniqueId().toString().replace("-", "");

        // Replace any existing notification for this player
        countdownApi.stopCountdown(countdownId);
        countdownApi.deleteCountdown(countdownId);

        final String template = configManager.getMainConfig().getString(
                "notifications.bossbar.message",
                "&6\u2b06 {player} reached {skill} Level {level}!");
        final String message = template
                .replace("{player}", event.getPlayer().getName())
                .replace("{skill}", capitalize(event.getSkillType().name()))
                .replace("{level}", String.valueOf(event.getNewLevel()));

        final Countdown countdown = CountdownBuilder.builder(countdownId)
                .type(CountdownType.DURATION)
                .displayTypes(EnumSet.of(DisplayType.BOSS_BAR))
                .updateIntervalSeconds(1)
                .formatMessage(message)
                .zoneId(ZoneId.systemDefault())
                .duration(Duration.ofSeconds(durationSeconds))
                .build();

        countdownApi.createCountdown(countdown);
        countdownApi.startCountdown(countdownId);
    }

    /**
     * Deletes the EzSkills-created countdown from EzCountdown storage once it expires,
     * preventing stale entries from accumulating in {@code countdowns.yml}.
     *
     * @param event the countdown end event
     */
    @EventHandler
    public void onCountdownEnd(@NotNull CountdownEndEvent event) {
        if (event.getCountdown().getName().startsWith(ID_PREFIX)) {
            countdownApi.deleteCountdown(event.getCountdown().getName());
        }
    }

    /**
     * Converts a skill enum name (e.g. {@code "WOODCUTTING"}) to title-case
     * (e.g. {@code "Woodcutting"}).
     *
     * @param name the raw enum constant name
     * @return a capitalised, space-separated string
     */
    private static String capitalize(@NotNull String name) {
        final String lower = name.toLowerCase(Locale.ROOT).replace('_', ' ');
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
