package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = listOf(SkillType.PASSIVE),
    rarity = SkillRarity.SUPERB,
    cooldown = 10,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME)
), AutoTrigger, AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override val persistTime: Int = 10 * 20

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > it.maxDamage / 2 }

    override fun onStop(player: ServerPlayerEntity) {
        player.armorItems.filter { it.isDamaged && it.damage > it.maxDamage / 2 }.forEach { it.damage -= 1 }
        super.onStop(player)
    }
}