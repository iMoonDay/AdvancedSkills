package com.imoonday.skill

import com.imoonday.trigger.CooldownTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.server.network.ServerPlayerEntity

class MasterySkill : Skill(
    id = "mastery",
    types = arrayOf(SkillType.PASSIVE),
    rarity = Rarity.LEGENDARY
), CooldownTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)
    override fun getCooldown(original: Int): Int = (original * 0.8).toInt()
}