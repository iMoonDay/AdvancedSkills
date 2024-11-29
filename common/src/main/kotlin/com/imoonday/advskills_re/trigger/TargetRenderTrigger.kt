package com.imoonday.advskills_re.trigger

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface TargetRenderTrigger : RendererTrigger {

    fun isTarget(player: PlayerEntity, entity: LivingEntity): Boolean
}