package com.imoonday.entity.render

import com.imoonday.entity.*
import com.imoonday.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class SilenceEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<SilenceEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/silence_energy_ball.png")
}