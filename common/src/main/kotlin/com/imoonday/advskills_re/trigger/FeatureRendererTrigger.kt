package com.imoonday.advskills_re.trigger

import com.imoonday.advskills_re.util.*
import net.minecraft.client.util.*
import net.minecraft.entity.player.*

interface FeatureRendererTrigger : RendererTrigger {

    fun shouldRenderFeature(target: PlayerEntity, player: PlayerEntity): Boolean

    fun getRenderModel(target: PlayerEntity, player: PlayerEntity): ModelIdentifier = getAsSkill().modelId
}