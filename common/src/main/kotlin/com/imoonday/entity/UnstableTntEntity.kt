package com.imoonday.entity

import com.imoonday.init.ModEntities
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.TntEntity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class UnstableTntEntity(entityType: EntityType<out UnstableTntEntity>, world: World) : TntEntity(entityType, world) {

    init {
        noClip = true
    }

    constructor(
        world: World,
        x: Double,
        y: Double,
        z: Double,
        igniter: LivingEntity?,
        velocity: Vec3d,
    ) : this(ModEntities.UNSTABLE_TNT, world) {
        this.setPosition(x, y, z)
        this.prevX = x
        this.prevY = y
        this.prevZ = z
        this.causingEntity = igniter
        this.velocity = velocity
        this.fuse = 20 * 10
    }

    override fun tick() {
        super.tick()
        val collisions = world.getCollisions(this, boundingBox).toList()
        if (collisions.isNotEmpty()) {
            this.discard()
            if (!world.isClient) {
                explode()
            }
        }
    }
}