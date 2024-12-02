package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.particle.*
import net.minecraft.server.world.*
import net.minecraft.sound.*
import net.minecraft.world.*
import org.joml.*
import kotlin.random.Random

class FreezeEnergyBallEntity(entityType: EntityType<out FreezeEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(ModEffects.FREEZE.get(), 3 * 20, 0, false, false, true) to 0.3f,
    )
    override var range: Double = 4.0

    constructor(
        x: Double,
        y: Double,
        z: Double,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
        world: World,
    ) : this(ModEntities.FREEZE_ENERGY_BALL.get(), world) {
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

    override fun spawnParticles() {
        (world as? ServerWorld)?.spawnParticles(
            DustParticleEffect(Companion.particleColor, 1f),
            x,
            y,
            z,
            (range * range * 100).toInt(),
            range - 1,
            range - 1,
            range - 1,
            0.0
        )
    }

    override fun playSound() =
        world.playSound(null, blockPos, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.VOICE)

    override fun canApply(effect: StatusEffectInstance, chance: Float, target: LivingEntity): Boolean =
        target.isWet && Random.nextFloat() < chance * 2f

    companion object {

        private val particleColor = Vector3f(178 / 255f, 1f, 1f)
    }
}