package com.imoonday.advskills_re.skill.trigger.client.render

import net.minecraft.entity.player.*

interface UsingRenderTrigger : FeatureRendererTrigger {

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean = target.isUsing()
}