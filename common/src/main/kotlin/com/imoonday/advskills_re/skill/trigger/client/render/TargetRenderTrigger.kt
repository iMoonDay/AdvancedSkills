package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.*
import net.minecraft.entity.player.*

interface TargetRenderTrigger : RenderTrigger {

    fun isTarget(clientPlayer: PlayerEntity, entity: LivingEntity): Boolean
}