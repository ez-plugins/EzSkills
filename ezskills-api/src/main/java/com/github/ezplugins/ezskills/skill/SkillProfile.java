package com.github.ezplugins.ezskills.skill;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * Contains {@link SkillProgress} entries for every {@link SkillType} for a single player.
 */
public final class SkillProfile {

    /** Stores the skill progress for each skill type. */
    private final EnumMap<SkillType, SkillProgress> skills;

    public SkillProfile() {
        skills = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            skills.put(type, new SkillProgress());
        }
    }

    /**
     * Returns the {@link SkillProgress} for the given type. Never {@code null}.
     *
     * @param type the skill type
     * @return the progress object
     */
    public SkillProgress getProgress(SkillType type) {
        return skills.get(type);
    }

    /**
     * Returns an immutable view of all skill progress entries.
     *
     * @return unmodifiable map
     */
    public Map<SkillType, SkillProgress> getAll() {
        return Collections.unmodifiableMap(skills);
    }
}
