package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LivingDetectionSkill : Skill(
    id = "living_detection",
    types = listOf(SkillType.FUNCTION),
    cooldown = 20,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, GlowingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override val persistTime: Int = 5 * 20

    override fun isGlowing(entity: Entity, player: PlayerEntity): Boolean =
        player.isUsing() && entity != player && entity.isLiving && entity.isAlive && player.distanceTo(entity) <= 50 && (entity.x != entity.prevX || entity.y != entity.prevY || entity.z != entity.prevZ)
}