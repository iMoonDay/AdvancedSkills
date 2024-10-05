package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class TemporaryShieldSkill : Skill(
    id = "temporary_shield",
    types = listOf(SkillType.DEFENSE),
    cooldown = 30,
    rarity = Rarity.LEGENDARY
), AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getPersistTime(): Int = 20 * 10

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && usedTime % 20 == 0) player.absorptionAmount =
            (player.absorptionAmount + 1).coerceAtMost(10f)
        super.serverTick(player, usedTime)
    }
}