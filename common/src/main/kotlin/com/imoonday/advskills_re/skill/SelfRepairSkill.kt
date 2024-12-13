package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*

class SelfRepairSkill : Skill(
    id = "self_repair",
    types = listOf(SkillType.PASSIVE),
    rarity = SkillRarity.SUPERB,
    cooldown = 10,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.EFFECT_VALUE)
), AutoTrigger, AutoStopTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override val persistTime: Int = 10 * 20

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        player.armorItems.filter { it.isDamaged }.any { it.damage > getMaxRepairLimit(player, it) }

    override fun onStop(player: ServerPlayerEntity) {
        player.armorItems.filter { it.isDamaged && it.damage > getMaxRepairLimit(player, it) }
            .forEach { it.damage -= 1 }
        super.onStop(player)
    }

    fun getMaxRepairLimit(player: PlayerEntity, stack: ItemStack): Int =
        (stack.maxDamage * (0.5 - player.getEnhancementLvl(SkillEnhancements.EFFECT_VALUE) * 0.1)).toInt()
            .coerceAtLeast(0)
}