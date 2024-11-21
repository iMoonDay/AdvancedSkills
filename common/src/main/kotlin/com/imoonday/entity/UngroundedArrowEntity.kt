package com.imoonday.entity

import com.imoonday.init.*
import net.minecraft.entity.*
import net.minecraft.entity.projectile.*
import net.minecraft.world.*

class UngroundedArrowEntity(entityType: EntityType<out UngroundedArrowEntity>, world: World) :
    ArrowEntity(entityType, world) {

    constructor(
        world: World,
        x: Double,
        y: Double,
        z: Double,
        owner: Entity
    ) : this(ModEntities.UNGROUNDED_ARROW.get(), world) {
        this.setPosition(x, y, z)
        this.owner = owner
        this.pickupType = PickupPermission.CREATIVE_ONLY
    }

    override fun tick() {
        super.tick()
        if (inGround) discard()
    }

    override fun canHit(entity: Entity): Boolean = super.canHit(entity) && entity !== owner
}