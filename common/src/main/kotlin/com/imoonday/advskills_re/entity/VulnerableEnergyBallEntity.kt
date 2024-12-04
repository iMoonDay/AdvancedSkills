package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.particle.*
import net.minecraft.util.math.*
import net.minecraft.world.*

class VulnerableEnergyBallEntity(entityType: EntityType<out VulnerableEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var range: Double = 5.0

    constructor(
        x: Double,
        y: Double,
        z: Double,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
        world: World,
    ) : this(ModEntities.VULNERABLE_ENERGY_BALL.get(), world) {
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

    override fun getEffects(): Map<StatusEffectInstance, Float> {
        val effect = ModEffects.VULNERABLE.get()
        return linkedMapOf(
            StatusEffectInstance(effect, 8 * 20, 0, false, false, true) to 0.8f,
            StatusEffectInstance(effect, 8 * 20, 1, false, false, true) to 0.4f,
            StatusEffectInstance(effect, 8 * 20, 2, false, false, true) to 0.2f,
            StatusEffectInstance(effect, 8 * 20, 3, false, false, true) to 0.1f,
        )
    }

    override fun getExplosionParticle(): ParticleEffect = DustParticleEffect(particleColor, 1f)

    companion object {

        private val particleColor = Vec3d.unpackRgb(16738740).toVector3f()
    }
}