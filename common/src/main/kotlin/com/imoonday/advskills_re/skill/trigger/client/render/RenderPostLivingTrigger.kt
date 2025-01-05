package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface RenderPostLivingTrigger : RenderTrigger {

    fun shouldRenderPostLiving(living: LivingEntity, player: PlayerEntity): Boolean
}