package com.imoonday.entity

import com.imoonday.entity.render.EffectEnergyBallEntityRenderer
import com.imoonday.init.ModEntities
import com.imoonday.util.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.Identifier
import net.minecraft.world.World

class SlownessEnergyBallEntity(entityType: EntityType<out SlownessEnergyBallEntity>, world: World) :
    EffectEnergyBallEntity(entityType, world) {

    override var effects = mutableMapOf(
        StatusEffectInstance(StatusEffects.SLOWNESS, 20 * 8, 1) to 0.5f,
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
    ) : this(ModEntities.SLOWNESS_ENERGY_BALL, world) {
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
        EffectEnergyBallEntityRenderer<SlownessEnergyBallEntity>(context) {

        override val texture: Identifier = id("textures/entity/freeze_energy_ball.png")
        override var scale = 1f
    }
}