package com.imoonday.advskills_re.client.render.entity

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class SlownessEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<SlownessEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
    override var scale = 1f
}