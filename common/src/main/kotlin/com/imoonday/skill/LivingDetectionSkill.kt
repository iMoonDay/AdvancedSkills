package com.imoonday.skill

import com.imoonday.trigger.*
import com.imoonday.util.*
import net.minecraft.entity.*
import net.minecraft.server.network.*

class LivingDetectionSkill : Skill(
    id = "living_detection",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 20,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, GlowingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override val persistTime: Int = 20 * 5

    override fun isGlowing(entity: Entity): Boolean {
        val player = clientPlayer ?: return false
        return player.isUsing() && entity != player && entity.isLiving && entity.isAlive && player.distanceTo(entity) <= 50 && (entity.x != entity.prevX || entity.y != entity.prevY || entity.z != entity.prevZ)
    }
}