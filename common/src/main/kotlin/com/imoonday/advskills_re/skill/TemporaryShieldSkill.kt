package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class TemporaryShieldSkill : Skill(
    id = "temporary_shield",
    types = listOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), AutoStopTrigger {

    override val persistTime: Int = 10 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this).withCooling(true)

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && usedTime % 20 == 0) player.absorptionAmount =
            (player.absorptionAmount + 1).coerceAtMost(10f)
        super.serverTick(player, usedTime)
    }
}