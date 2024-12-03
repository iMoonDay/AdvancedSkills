package com.imoonday.advskills_re.trigger.renderer

import com.imoonday.advskills_re.client.*
import net.minecraft.client.util.*
import net.minecraft.entity.player.*

interface FeatureRendererTrigger : RendererTrigger {

    fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean

    fun getRenderModel(target: PlayerEntity, player: PlayerEntity): ModelIdentifier = getAsSkill().modelId
}