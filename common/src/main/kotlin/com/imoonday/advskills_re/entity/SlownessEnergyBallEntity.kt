package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.world.*

class SlownessEnergyBallEntity(entityType: EntityType<out SlownessEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(StatusEffects.SLOWNESS, 8 * 20, 1) to 0.5f,
    )
    override var range: Double = 5.0

    constructor(
        x: Double,
        y: Double,
        z: Double,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
        world: World,
    ) : this(ModEntities.SLOWNESS_ENERGY_BALL.get(), world) {
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