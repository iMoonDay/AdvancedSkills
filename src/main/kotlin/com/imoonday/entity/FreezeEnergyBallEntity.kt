package com.imoonday.entity

import com.imoonday.entity.render.EffectEnergyBallEntityRenderer
import com.imoonday.init.ModEffects
import com.imoonday.init.ModEntities
import com.imoonday.util.id
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.util.Identifier
import net.minecraft.world.World

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
}