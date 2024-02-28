package com.imoonday.skills

import com.imoonday.triggers.CooldownTrigger
import com.imoonday.utils.Skill
import com.imoonday.utils.SkillType
import com.imoonday.utils.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class MasterySkill : Skill(
    id = "mastery",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.LEGENDARY
), CooldownTrigger {
    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)
    override fun getCooldown(original: Int): Int = (original * 0.8).toInt()
}