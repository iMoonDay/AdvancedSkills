package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.server.network.*

class MasterySkill : Skill(
    id = "mastery",
    types = listOf(SkillType.PASSIVE),
    rarity = SkillRarity.LEGENDARY
), CooldownTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)
    override fun getCooldown(original: Int): Int = (original * 0.8).toInt()
}