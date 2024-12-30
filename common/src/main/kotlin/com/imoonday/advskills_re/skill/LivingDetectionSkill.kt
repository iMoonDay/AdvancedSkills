package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LivingDetectionSkill : Skill(
    id = "living_detection",
    types = listOf(SkillType.UTILITY),
    cooldown = 20,
    rarity = SkillRarity.SUPERB,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.RANGE)
), AutoStopTrigger, GlowingTrigger {

    init {
        addEnhanceableParameter(
            name = timeParamName,
            baseValue = 5 * 20,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5
        ) { (it * 100).toInt() }
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun isGlowing(entity: Entity, clientPlayer: PlayerEntity): Boolean {
        val range = 50 * (1.0 + clientPlayer.getEnhancementLvl(SkillEnhancements.RANGE) * 0.2)
        return clientPlayer.isUsing()
            && entity != clientPlayer
            && entity.isLiving
            && entity.isAlive
            && clientPlayer.distanceTo(entity) <= range
            && (entity.x != entity.prevX
            || entity.y != entity.prevY
            || entity.z != entity.prevZ)
    }

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }
}