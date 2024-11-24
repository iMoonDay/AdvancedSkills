package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class UnhinderedStrideSkill : Skill(
    id = "unhindered_stride",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB
), StepHeightTrigger, AutoStopTrigger {

    override val persistTime: Int = 20 * 10

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getStepHeight(player: PlayerEntity): Float? =
        if (player.isUsing()) player.world.height.toFloat() else null
}