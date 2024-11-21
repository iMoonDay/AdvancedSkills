package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.SkillType
import net.minecraft.entity.player.*

class AutomaticUphillSkill : PassiveSkill(
    id = "automatic_uphill",
    types = listOf(SkillType.PASSIVE),
    rarity = Rarity.RARE,
    toggleable = true
), StepHeightTrigger, PersistentTrigger {

    override fun getStepHeight(player: PlayerEntity): Float? = if (!player.isUsing()) null else 1.0f
}