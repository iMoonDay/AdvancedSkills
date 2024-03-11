package com.imoonday.entity

import com.imoonday.entity.render.EffectEnergyBallEntityRenderer
import com.imoonday.init.ModEffects
import com.imoonday.init.ModEntities
import com.imoonday.util.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.particle.DustParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.world.World
import org.joml.Vector3f
import kotlin.random.Random

class FreezeEnergyBallEntity(entityType: EntityType<out FreezeEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(ModEffects.FREEZE, 20 * 3, 0, false, false, true) to 0.3f,
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
    ) : this(ModEntities.FREEZE_ENERGY_BALL, world) {
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
        EffectEnergyBallEntityRenderer<FreezeEnergyBallEntity>(context) {

        override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
    }

    private val particleColor = Vector3f(178 / 255f, 1f, 1f)

    override fun spawnParticles() {
        (world as? ServerWorld)?.spawnParticles(
            DustParticleEffect(particleColor, 1f),
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
}