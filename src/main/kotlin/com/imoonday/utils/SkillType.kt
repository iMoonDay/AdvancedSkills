package com.imoonday.utils

import net.minecraft.text.Text

enum class SkillType(val translationKey: String) {
    ATTACK("advancedSkills.skillType.attack"),
    DEFENSE("advancedSkills.skillType.defense"),
    HEALING("advancedSkills.skillType.healing"),
    CONTROL("advancedSkills.skillType.control"),
    PASSIVE("advancedSkills.skillType.passive"),
    ENHANCEMENT("advancedSkills.skillType.enhancement"),
    SUMMON("advancedSkills.skillType.summon"),
    RESTORATION("advancedSkills.skillType.restoration"),
    MOVEMENT("advancedSkills.skillType.movement"),
    DESTRUCTION("advancedSkills.skillType.destruction");

    val displayName: Text
        get() = Text.translatable(translationKey)
}