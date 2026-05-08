package com.github.ezplugins.ezskills.skill;

import com.github.ezframework.jaloquent.model.Model;
import java.util.List;
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
 *   <li>{@code acrobatics_level}, {@code acrobatics_experience}</li>
 *   <li>Custom skills follow the same {@code <name>_level} / {@code <name>_experience} pattern.</li>
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
     * Creates a {@link SkillProfileModel} populated from the given {@link SkillProfile},
     * including any custom skills stored in the profile.
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
        for (var entry : profile.getAllCustom().entrySet()) {
            final String prefix = entry.getKey().toLowerCase();
            model.set(prefix + "_level", entry.getValue().getLevel());
            model.set(prefix + "_experience", entry.getValue().getExperience());
        }
        return model;
    }

    /**
     * Converts this model back into a {@link SkillProfile}, also restoring progress
     * for all custom skills provided by the registry.
     *
     * @param customSkillNames upper-case names of all registered custom skills
     * @return the reconstructed profile
     */
    @NotNull
    public SkillProfile toSkillProfile(@NotNull List<String> customSkillNames) {
        final SkillProfile profile = new SkillProfile();
        for (SkillType type : SkillType.values()) {
            final String prefix = type.name().toLowerCase();
            final SkillProgress progress = profile.getProgress(type);
            progress.setLevel(getAs(prefix + "_level", Integer.class, 1));
            progress.setExperience(getAs(prefix + "_experience", Double.class, 0.0));
        }
        for (String skillName : customSkillNames) {
            final String prefix = skillName.toLowerCase();
            final SkillProgress progress = profile.getCustomProgress(skillName);
            progress.setLevel(getAs(prefix + "_level", Integer.class, 1));
            progress.setExperience(getAs(prefix + "_experience", Double.class, 0.0));
        }
        return profile;
    }

    /**
     * Converts this model back into a {@link SkillProfile} for built-in skills only.
     *
     * @return the reconstructed profile
     */
    @NotNull
    public SkillProfile toSkillProfile() {
        return toSkillProfile(List.of());
    }

}
