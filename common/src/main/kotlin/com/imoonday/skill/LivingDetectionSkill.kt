package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.GlowingTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.Entity
import net.minecraft.server.network.ServerPlayerEntity

class LivingDetectionSkill : Skill(
    id = "living_detection",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 20,
    rarity = Rarity.SUPERB,
), AutoStopTrigger, GlowingTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun getPersistTime(): Int = 20 * 5

    override fun isGlowing(player: ClientPlayerEntity, entity: Entity): Boolean =
        player.isUsing() && entity != player && entity.isLiving && entity.isAlive && player.distanceTo(entity) <= 50 && (entity.x != entity.prevX || entity.y != entity.prevY || entity.z != entity.prevZ)
}