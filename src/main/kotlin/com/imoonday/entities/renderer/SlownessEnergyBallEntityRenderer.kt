package com.imoonday.entities.renderer

import com.imoonday.entities.SlownessEnergyBallEntity
import com.imoonday.utils.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier

class SlownessEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<SlownessEnergyBallEntity>(context) {
    override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
    override var scale = 1f
}