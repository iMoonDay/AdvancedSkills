package com.imoonday.entities.renderer

import com.imoonday.AdvancedSkills
import com.imoonday.entities.SilenceEnergyBallEntity
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.util.Identifier

class SilenceEnergyBallEntityRenderer(context: EntityRendererFactory.Context) :
    EffectEnergyBallEntityRenderer<SilenceEnergyBallEntity>(context) {
    override val texture: Identifier = AdvancedSkills.id("textures/entity/silence_energy_ball.png")
}