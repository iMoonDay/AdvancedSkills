package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class MasterySkill : Skill(
    Settings(
        id = "mastery",
        types = listOf(SkillType.PASSIVE),
        rarity = SkillRarity.LEGENDARY
    )
), CooldownTrigger {

    init {
        addParameter(
            name = "cooldown_multiplier",
            baseValue = 0.8,
            enhancementId = "multiplier",
            value = -0.06,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun getCooldown(player: PlayerEntity, original: Int): Int {
        val multiplier = getDoubleParam("cooldown_multiplier", player, 0.8, 0.0)
        return (original * multiplier).toInt()
    }
}