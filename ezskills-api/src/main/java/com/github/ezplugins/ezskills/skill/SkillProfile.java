package com.github.ezplugins.ezskills.skill;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Contains {@link SkillProgress} entries for every {@link SkillType} for a single player,
 * plus an additional map for any custom skills registered at runtime.
 */
public final class SkillProfile {

    /** Stores the skill progress for each built-in skill type. */
    private final EnumMap<SkillType, SkillProgress> skills;

    /** Stores progress for custom (non-enum) skills, keyed by upper-case skill name. */
    private final Map<String, SkillProgress> customSkills = new HashMap<>();

    public SkillProfile() {
        skills = new EnumMap<>(SkillType.class);
        for (SkillType type : SkillType.values()) {
            skills.put(type, new SkillProgress());
        }
    }

    /**
     * Returns the {@link SkillProgress} for the given built-in type. Never {@code null}.
     *
     * @param type the skill type
     * @return the progress object
     */
    public SkillProgress getProgress(SkillType type) {
        return skills.get(type);
    }

    /**
     * Returns an immutable view of all built-in skill progress entries.
     *
     * @return unmodifiable map
     */
    public Map<SkillType, SkillProgress> getAll() {
        return Collections.unmodifiableMap(skills);
    }

    /**
     * Returns the {@link SkillProgress} for the given custom skill name, creating a fresh entry
     * at level 1 / 0 XP if none exists yet.
     *
     * @param skillName the skill name (compared case-insensitively)
     * @return the progress object
     */
    public SkillProgress getCustomProgress(String skillName) {
        return customSkills.computeIfAbsent(skillName.toUpperCase(), k -> new SkillProgress());
    }

    /**
     * Explicitly initialises a custom skill entry if none exists yet.
     * No-op if the skill already has an entry.
     *
     * @param skillName the skill name
     */
    public void initCustomSkill(String skillName) {
        customSkills.computeIfAbsent(skillName.toUpperCase(), k -> new SkillProgress());
    }

    /**
     * Returns an immutable view of all custom skill progress entries,
     * keyed by upper-case skill name.
     *
     * @return unmodifiable map
     */
    public Map<String, SkillProgress> getAllCustom() {
        return Collections.unmodifiableMap(customSkills);
    }
}
