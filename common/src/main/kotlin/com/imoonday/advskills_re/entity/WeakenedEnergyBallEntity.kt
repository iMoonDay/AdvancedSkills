package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.particle.*
import net.minecraft.util.math.*
import net.minecraft.world.*

class WeakenedEnergyBallEntity(entityType: EntityType<out WeakenedEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(ModEffects.WEAKENED.get(), 8 * 20, 0, false, false, true) to 0.8f,
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
    ) : this(ModEntities.WEAKENED_ENERGY_BALL.get(), world) {
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

    override fun getExplosionParticle(): ParticleEffect = DustParticleEffect(particleColor, 1f)

    companion object {

        private val particleColor = Vec3d.unpackRgb(16738740).toVector3f()
    }
}