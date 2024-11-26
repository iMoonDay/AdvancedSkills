package com.imoonday.advskills_re.util

import net.minecraft.text.*

enum class SkillType(val translationKey: String) {
    ATTACK("advskills_re.skillType.attack"),
    DEFENSE("advskills_re.skillType.defense"),
    FUNCTION("advskills_re.skillType.function"),
    CONTROL("advskills_re.skillType.control"),
    PASSIVE("advskills_re.skillType.passive"),
    ENHANCEMENT("advskills_re.skillType.enhancement"),
    SUMMON("advskills_re.skillType.summon"),
    RESTORATION("advskills_re.skillType.restoration"),
    MOVEMENT("advskills_re.skillType.movement"),
    DESTRUCTION("advskills_re.skillType.destruction");

    val displayName: Text
        get() = Text.translatable(translationKey)
}