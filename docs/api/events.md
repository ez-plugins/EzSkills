---
layout: default
title: Events
parent: Developer Docs
nav_order: 3
---

# Events

All EzSkills events are found in the package `com.github.ezplugins.ezskills.api.event`.

## SkillLevelUpEvent

Fired when a player's skill increases by one level.

```java
@EventHandler
public void onLevelUp(SkillLevelUpEvent event) {
    Player player   = event.getPlayer();
    SkillType skill = event.getSkillType();
    int oldLevel    = event.getOldLevel();
    int newLevel    = event.getNewLevel();

    player.sendMessage("You reached " + skill.name() + " level " + newLevel + "!");
}
```

## EzSkillsAbilityPrepareEvent

Fired when an ability enters the prepared (charged) state.

```java
@EventHandler
public void onPrepare(EzSkillsAbilityPrepareEvent event) {
    event.getPlayer().sendMessage(event.getAbilityType().name() + " is ready!");
}
```

## EzSkillsAbilityActivateEvent

Fired when an ability is activated.

## EzSkillsAbilityDeactivateEvent

Fired when an ability expires or is cancelled.
