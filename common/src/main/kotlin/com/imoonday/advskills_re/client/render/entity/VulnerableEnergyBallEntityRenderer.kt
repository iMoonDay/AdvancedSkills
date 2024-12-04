package com.imoonday.advskills_re.client.render.entity

import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.util.*

class VulnerableEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<VulnerableEnergyBallEntity>(context) {

    override val texture: Identifier = id("textures/entity/vulnerable_energy_ball.png")
}