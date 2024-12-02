package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class UnhinderedStrideSkill : Skill(
    id = "unhindered_stride",
    types = listOf(SkillType.ENHANCEMENT, SkillType.MOVEMENT),
    cooldown = 15,
    rarity = Rarity.SUPERB
), StepHeightTrigger, AutoStopTrigger {

    override val persistTime: Int = 10 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.toggleUsing(user, this)

    override fun getStepHeight(player: PlayerEntity): Float? =
        if (player.isUsing()) player.world.height.toFloat() else null
}