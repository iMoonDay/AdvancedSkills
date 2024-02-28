package com.imoonday.entities.renderer

import com.imoonday.entities.SilenceEnergyBallEntity
import com.imoonday.utils.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier

class SilenceEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<SilenceEnergyBallEntity>(context) {
    override val texture: Identifier = id("textures/entity/silence_energy_ball.png")
}