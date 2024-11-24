package com.imoonday.entity.render

import com.imoonday.entity.*
import com.imoonday.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class FreezeEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<FreezeEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
}