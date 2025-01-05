package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.player.*

interface FeatureRendererTrigger : RenderTrigger {

    fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean
}