package com.imoonday.entity

import com.imoonday.entity.render.*
import com.imoonday.init.*
import com.imoonday.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.util.*
import net.minecraft.world.*

class SilenceEnergyBallEntity(entityType: EntityType<out SilenceEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(ModEffects.SILENCE.get(), 20 * 5, 0, false, false, true) to 0.5f,
        StatusEffectInstance(StatusEffects.SLOWNESS, 30, 2, false, false, true) to 1f
    )
    override var range: Double = 2.0

    constructor(
        x: Double,
        y: Double,
        z: Double,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
        world: World,
    ) : this(ModEntities.SILENCE_ENERGY_BALL.get(), world) {
        update(x, y, z, directionX, directionY, directionZ)
    }

    constructor(
        owner: LivingEntity,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
        world: World,
    ) : this(owner.x, owner.y, owner.z, directionX, directionY, directionZ, world) {
        update(owner)
    }

    class Renderer(context: EntityRendererFactory.Context) :
        EffectEnergyBallEntityRenderer<SilenceEnergyBallEntity>(context) {

        override val texture: Identifier = id("textures/entity/silence_energy_ball.png")
    }
}