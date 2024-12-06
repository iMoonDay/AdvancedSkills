package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class AutomaticUphillSkill : PassiveSkill(
    id = "automatic_uphill",
    extraTypes = listOf(SkillType.MOVEMENT),
    rarity = SkillRarity.RARE,
    toggleable = true
), StepHeightTrigger, PersistentTrigger {

    override fun getStepHeight(player: PlayerEntity): Float? = if (player.isUsing()) 1.0f else null

    override fun keepUsingAfterRespawn(player: ServerPlayerEntity): Boolean = true
}