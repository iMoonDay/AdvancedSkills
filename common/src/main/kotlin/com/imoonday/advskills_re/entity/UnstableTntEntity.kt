package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import net.minecraft.entity.*
import net.minecraft.util.math.*
import net.minecraft.world.*

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
    ) : this(ModEntities.UNSTABLE_TNT.get(), world) {
        this.setPosition(x, y, z)
        this.prevX = x
        this.prevY = y
        this.prevZ = z
        this.causingEntity = igniter
        this.velocity = velocity
        this.fuse = 10 * 20
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