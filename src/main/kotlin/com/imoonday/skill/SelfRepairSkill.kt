package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.AutoTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.SUPERB,
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