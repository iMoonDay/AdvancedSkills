package com.imoonday.advskills_re.entity

import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.projectile.*
import net.minecraft.nbt.*
import net.minecraft.particle.*
import net.minecraft.server.world.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.world.*
import kotlin.math.*

private const val EFFECT_RANGE_KEY = "EffectRange"

abstract class EffectEnergyBallEntity(entityType: EntityType<out EffectEnergyBallEntity>, world: World) :
    ExplosiveProjectileEntity(entityType, world) {

    abstract var range: Double

    abstract fun getEffects(): Map<StatusEffectInstance, Float>

    protected fun update(
        x: Double,
        y: Double,
        z: Double,
        directionX: Double,
        directionY: Double,
        directionZ: Double,
    ) {
        this.refreshPositionAndAngles(x, y, z, this.yaw, this.pitch)
        this.refreshPosition()
        val d = sqrt(directionX * directionX + directionY * directionY + directionZ * directionZ)
        if (d != 0.0) {
            this.powerX = directionX / d * 0.1
            this.powerY = directionY / d * 0.1
            this.powerZ = directionZ / d * 0.1
        }
    }

    protected fun update(owner: LivingEntity) {
        this.owner = owner
        this.setRotation(owner.yaw, owner.pitch)
    }

    override fun onCollision(hitResult: HitResult?) {
        super.onCollision(hitResult)
        if (world.isClient) return
        val effects = getEffects()
        if (effects.isNotEmpty()) {
            world.getOtherEntities(null, this.boundingBox.expand(range)) { it is LivingEntity }
                .filterIsInstance<LivingEntity>().forEach {
                    for (entry in effects) {
                        val effect = entry.key
                        val chance = entry.value
                        if (random.nextFloat() < chance || canApply(effect, chance, it)) {
                            it.addStatusEffect(effect, effectCause)
                        }
                    }
                }
            spawnParticles()
            playSound()
        }
        discard()
    }

    protected open fun playSound() =
        world.playSound(null, blockPos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.VOICE)

    protected open fun spawnParticles() {
        (world as? ServerWorld)?.spawnParticles(
            getExplosionParticle(),
            x, y, z, (range * range * 100).toInt(),
            range - 1, range - 1, range - 1, 0.0
        )
    }

    protected open fun getExplosionParticle(): ParticleEffect = ParticleTypes.EFFECT

    open fun canApply(effect: StatusEffectInstance, chance: Float, target: LivingEntity): Boolean = false

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putDouble(EFFECT_RANGE_KEY, range)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains(EFFECT_RANGE_KEY, NbtElement.NUMBER_TYPE.toInt())) {
            range = nbt.getDouble(EFFECT_RANGE_KEY)
        }
    }

    override fun isBurning(): Boolean = false

    override fun canHit(): Boolean = false

    override fun getDrag(): Float = 1.0f

    override fun isTouchingWater(): Boolean = false
}