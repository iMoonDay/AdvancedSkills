package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import net.minecraft.entity.player.*

class AutomaticUphillSkill : PassiveSkill(
    id = "automatic_uphill",
    types = listOf(SkillType.PASSIVE),
    rarity = Rarity.RARE,
    toggleable = true
), StepHeightTrigger, PersistentTrigger {

    override fun getStepHeight(player: PlayerEntity): Float? = if (player.isUsing()) 1.0f else null
}