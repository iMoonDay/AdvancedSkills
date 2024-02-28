package com.imoonday.entities.renderer

import com.imoonday.entities.FreezeEnergyBallEntity
import com.imoonday.utils.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier

class FreezeEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<FreezeEnergyBallEntity>(context) {
    override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
}