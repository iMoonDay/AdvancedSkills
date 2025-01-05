package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface UsingRenderTrigger : FeatureRendererTrigger, RenderPostLivingTrigger {

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean = target.isUsing()

    override fun shouldRenderPostLiving(living: LivingEntity, player: PlayerEntity): Boolean = player.isUsing()
}