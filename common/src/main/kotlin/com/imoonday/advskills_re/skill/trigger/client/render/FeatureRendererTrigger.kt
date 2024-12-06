package com.imoonday.advskills_re.skill.trigger.client.render

import com.imoonday.advskills_re.client.*
import net.minecraft.client.util.*
import net.minecraft.entity.player.*

interface FeatureRendererTrigger : RenderTrigger {

    fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean

    fun getRenderModel(target: PlayerEntity, clientPlayer: PlayerEntity): ModelIdentifier = getAsSkill().modelId
}