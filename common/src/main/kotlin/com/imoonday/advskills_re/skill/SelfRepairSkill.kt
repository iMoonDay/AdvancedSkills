package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.SkillType
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = listOf(SkillType.PASSIVE),
    rarity = Rarity.SUPERB,
    cooldown = 10,
), AutoTrigger, AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override val persistTime: Int = 20 * 10

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > it.maxDamage / 2 }

    override fun onStop(player: ServerPlayerEntity) {
        player.armorItems.filter { it.isDamaged && it.damage > it.maxDamage / 2 }.forEach { it.damage -= 1 }
        super.onStop(player)
    }
}