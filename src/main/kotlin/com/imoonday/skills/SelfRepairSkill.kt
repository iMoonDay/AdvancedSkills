package com.imoonday.skills

import com.imoonday.triggers.AutoStopTrigger
import com.imoonday.triggers.AutoTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.VERY_RARE,
    cooldown = 10,
), AutoTrigger, AutoStopTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override val persistTime: Int = 20 * 10

    override val skill: Skill
        get() = this

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > it.maxDamage / 2 }

    override fun onStop(player: ServerPlayerEntity) {
        player.armorItems.filter { it.isDamaged && it.damage > it.maxDamage / 2 }.forEach { it.damage -= 1 }
        super.onStop(player)
    }
}