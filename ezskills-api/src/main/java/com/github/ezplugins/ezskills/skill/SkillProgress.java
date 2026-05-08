package com.github.ezplugins.ezskills.skill;

/**
 * Holds the level and accumulated experience for a single skill.
 */
public final class SkillProgress {

    /** The current level. */
    private int level;

    /** The accumulated experience towards the next level. */
    private double experience;

    public SkillProgress() {
        this.level = 1;
        this.experience = 0.0;
    }

    public SkillProgress(int level, double experience) {
        this.level = level;
        this.experience = experience;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public double getExperience() {
        return experience;
    }

    public void setExperience(double experience) {
        this.experience = experience;
    }

    @Override
    public String toString() {
        return "SkillProgress{level=" + level + ", experience=" + experience + '}';
    }
}
