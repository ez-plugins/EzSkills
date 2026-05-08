package com.github.ezplugins.ezskills.skill;

import com.github.ezframework.jaloquent.model.Model;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Jaloquent {@link Model} representing one player's skill data as a flat row.
 *
 * <p>Column layout (one per skill type):</p>
 * <ul>
 *   <li>{@code woodcutting_level}, {@code woodcutting_experience}</li>
 *   <li>{@code mining_level}, {@code mining_experience}</li>
 *   <li>{@code fishing_level}, {@code fishing_experience}</li>
 *   <li>{@code fighting_level}, {@code fighting_experience}</li>
 * </ul>
 *
 * <p>The model {@code id} is the player's UUID string.</p>
 */
public final class SkillProfileModel extends Model {

    public SkillProfileModel(@NotNull String id) {
        super(id);
    }

    // -------------------------------------------------------------------------
    // Conversion helpers
    // -------------------------------------------------------------------------

    /**
     * Creates a {@link SkillProfileModel} populated from the given {@link SkillProfile}.
     *
     * @param playerId the player UUID
     * @param profile  the profile to read from
     * @return the new model
     */
    @NotNull
    public static SkillProfileModel fromSkillProfile(@NotNull UUID playerId,
                                                     @NotNull SkillProfile profile) {
        final SkillProfileModel model = new SkillProfileModel(playerId.toString());
        for (SkillType type : SkillType.values()) {
            final String prefix = type.name().toLowerCase();
            final SkillProgress progress = profile.getProgress(type);
            model.set(prefix + "_level", progress.getLevel());
            model.set(prefix + "_experience", progress.getExperience());
        }
        return model;
    }

    /**
     * Converts this model back into a {@link SkillProfile}.
     *
     * @return the reconstructed profile
     */
    @NotNull
    public SkillProfile toSkillProfile() {
        final SkillProfile profile = new SkillProfile();
        for (SkillType type : SkillType.values()) {
            final String prefix = type.name().toLowerCase();
            final SkillProgress progress = profile.getProgress(type);
            progress.setLevel(getAs(prefix + "_level", Integer.class, 1));
            progress.setExperience(getAs(prefix + "_experience", Double.class, 0.0));
        }
        return profile;
    }

}
