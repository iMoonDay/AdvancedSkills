package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*

class AutomaticUphillSkill : PassiveSkill(
    Settings(
        id = "automatic_uphill",
        types = listOf(SkillType.MOVEMENT),
        rarity = SkillRarity.RARE
    ), true
//    enhancements = setOf(SkillEnhancements.EFFECT_VALUE)
), StepHeightTrigger, PersistentTrigger {

    init {
        addEnhancementTooltipWithArg(SkillEnhancements.EFFECT_VALUE) { it.level * 0.5f }
    }

    override fun getStepHeight(player: PlayerEntity): Float? =
        if (player.isAvailable()) 1.0f + player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.5f else null
}