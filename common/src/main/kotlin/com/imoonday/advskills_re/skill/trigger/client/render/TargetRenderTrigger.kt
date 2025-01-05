package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface TargetRenderTrigger : RenderPostLivingTrigger {

    fun isTarget(clientPlayer: PlayerEntity, entity: LivingEntity): Boolean

    override fun shouldRenderPostLiving(living: LivingEntity, player: PlayerEntity): Boolean = isTarget(player, living)
}