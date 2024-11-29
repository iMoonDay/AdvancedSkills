package com.imoonday.advskills_re.trigger

import net.minecraft.entity.player.*

interface FeatureRendererTrigger : RendererTrigger {

    fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean = target.isUsing()
}