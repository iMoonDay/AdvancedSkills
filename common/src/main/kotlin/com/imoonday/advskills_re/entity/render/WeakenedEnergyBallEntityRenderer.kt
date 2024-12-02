package com.imoonday.advskills_re.entity.render

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class WeakenedEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<WeakenedEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/weakened_energy_ball.png")
}