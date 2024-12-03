package com.imoonday.advskills_re.trigger.renderer

import net.minecraft.entity.player.*

interface UsingRenderTrigger : FeatureRendererTrigger {

    override fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean = target.isUsing()
}