package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class LightLandingSkill : Skill(
    Settings(
        id = "light_landing",
        types = listOf(SkillType.DEFENSE, SkillType.UTILITY, SkillType.ENHANCEMENT),
        cooldown = 20,
        rarity = SkillRarity.RARE,
    )
), FallTrigger {

    override fun initDefaultSettings(settings: Settings) {
        TODO("Not yet implemented")
    }

    override fun use(user: ServerPlayerEntity): UseResult {
        TODO("Not yet implemented")
    }

    override fun onFall(amount: Int, player: ServerPlayerEntity, fallDistance: Float, damageMultiplier: Float): Int {
        return super.onFall(amount, player, fallDistance, damageMultiplier)
    }
}