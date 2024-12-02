package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.world.*

class SilenceEnergyBallEntity(entityType: EntityType<out SilenceEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(ModEffects.SILENCE.get(), 5 * 20, 0, false, false, true) to 0.5f,
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
}