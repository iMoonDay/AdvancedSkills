package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.server.network.*

class SpaceBlastSkill : LongPressSkill(
    id = "space_blast",
    types = listOf(SkillType.DESTRUCTION, SkillType.ATTACK),
    cooldown = 45,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(
        SkillEnhancements.RANGE,
        SkillEnhancements.DAMAGE,
        SkillEnhancements.CHARGE_TIME,
        SkillEnhancements.CHARGE_SLOWDOWN,
        SkillEnhancements.DISTANCE
    )
) {

    override fun getMaxPressTime(): Int = 20 * 5

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        TODO("Not yet implemented")
    }
}