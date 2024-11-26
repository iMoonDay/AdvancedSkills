package com.imoonday.advskills_re.entity.render

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class FreezeEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<FreezeEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
}