package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class MasterySkill : Skill(
    id = "mastery",
    types = listOf(SkillType.PASSIVE),
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(SkillEnhancements.EFFECT_VALUE)
), CooldownTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun getCooldown(player: PlayerEntity, original: Int): Int = (original * (0.8 - player.getEnhancementLvl(
        SkillEnhancements.EFFECT_VALUE
    ) * 0.06).coerceAtLeast(0.2)).toInt()
}