package com.imoonday.skill

import com.imoonday.trigger.PersistentTrigger
import com.imoonday.trigger.StepHeightTrigger
import com.imoonday.util.SkillType
import net.minecraft.entity.player.PlayerEntity

class AutomaticUphillSkill : PassiveSkill(
    id = "automatic_uphill",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.RARE,
    toggleable = true
), StepHeightTrigger, PersistentTrigger {
    override fun getStepHeight(player: PlayerEntity): Float? = if (!player.isUsing()) null else 1.0f
}