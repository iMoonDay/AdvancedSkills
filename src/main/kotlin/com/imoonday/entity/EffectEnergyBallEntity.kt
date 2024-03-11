package com.imoonday.entity

import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.projectile.ExplosiveProjectileEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import kotlin.math.sqrt

private const val EFFECTS_KEY = "Effects"
private const val EFFECT_RANGE_KEY = "EffectRange"
private const val CHANCE_KEY = "Chance"

abstract class EffectEnergyBallEntity(entityType: EntityType<out EffectEnergyBallEntity>, world: World) :
    ExplosiveProjectileEntity(entityType, world) {

    abstract val effects: MutableMap<StatusEffectInstance, Float>
    abstract var range: Double

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

    protected fun update(
        owner: LivingEntity,
    ) {
        this.owner = owner
        this.setRotation(owner.yaw, owner.pitch)
    }

    override fun onCollision(hitResult: HitResult?) {
        super.onCollision(hitResult)
        if (world.isClient) return
        if (effects.isNotEmpty()) {
            world.getOtherEntities(this, this.boundingBox.expand(range)) { it.isLiving && it.isAlive }
                .filterIsInstance<LivingEntity>()
                .forEach {
                    for (entry in effects) {
                        if (random.nextFloat() < entry.value || canApply(
                                entry.key,
                                entry.value,
                                it
                            )
                        ) it.addStatusEffect(
                            entry.key,
                            effectCause
                        )
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
            ParticleTypes.EFFECT,
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

    open fun canApply(effect: StatusEffectInstance, chance: Float, target: LivingEntity): Boolean = false

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putDouble(EFFECT_RANGE_KEY, range)
        if (effects.isNotEmpty()) {
            val nbtList = NbtList()
            for (entry in effects) {
                nbtList.add(entry.key.writeNbt(NbtCompound().apply { putFloat(CHANCE_KEY, entry.value) }))
            }
            nbt.put(EFFECTS_KEY, nbtList)
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains(EFFECT_RANGE_KEY, NbtElement.NUMBER_TYPE.toInt())) {
            range = nbt.getDouble(EFFECT_RANGE_KEY)
        }
        for (entry in getEffects(nbt)) {
            effects[entry.key] = entry.value
        }
    }

    override fun isBurning(): Boolean = false

    override fun canHit(): Boolean = false

    override fun getDrag(): Float = 1.0f

    override fun isTouchingWater(): Boolean = false

    fun getEffects(nbt: NbtCompound): MutableMap<StatusEffectInstance, Float> =
        mutableMapOf<StatusEffectInstance, Float>().apply {
            getEffects(nbt, this)
        }

    fun getEffects(nbt: NbtCompound, map: MutableMap<StatusEffectInstance, Float>) {
        if (nbt.contains(EFFECTS_KEY, NbtElement.LIST_TYPE.toInt())) {
            val nbtList = nbt.getList(EFFECTS_KEY, NbtElement.COMPOUND_TYPE.toInt())
            for (i in nbtList.indices) {
                val nbtCompound = nbtList.getCompound(i)
                val statusEffectInstance = StatusEffectInstance.fromNbt(nbtCompound) ?: continue
                map[statusEffectInstance] = nbtCompound.getFloat(CHANCE_KEY)
            }
        }
    }
}