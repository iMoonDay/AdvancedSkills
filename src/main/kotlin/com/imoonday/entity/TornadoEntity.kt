package com.imoonday.entity

import com.imoonday.init.ModEntities
import com.imoonday.util.blockPosSet
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

class TornadoEntity(type: EntityType<out ProjectileEntity>, world: World) :
    ProjectileEntity(type, world) {

    constructor(world: World, owner: Entity, velocity: Vec3d) : this(ModEntities.TORNADO, world) {
        this.owner = owner
        this.velocity = velocity
        noClip = true
    }

    override fun initDataTracker() = Unit

    override fun tick() {
        super.tick()
        if (!world.isClient && (age > 100 || !world.isChunkLoaded(blockPos))) {
            discard()
            return
        }
        move(MovementType.SELF, velocity)
        world.getNonSpectatingEntities(LivingEntity::class.java, boundingBox.expand(1.0)).forEach {
            if (it != owner) it.addVelocity(velocity)
        }
        val posSet = boundingBox.expand(1.0).blockPosSet
        posSet.filter { world.getBlockState(it).block.hardness == 0f }
            .forEach { world.breakBlock(it, true, this) }
        if (world.getBlockCollisions(this, boundingBox).toList().isNotEmpty()) {
            (world as? ServerWorld)?.spawnParticles(
                ParticleTypes.CLOUD,
                pos.x,
                pos.y + height / 2,
                pos.z,
                100,
                (width + 1) / 2.0,
                height / 2.0,
                (width + 1) / 2.0,
                0.0
            )
            discard()
        } else if (age % 5 == 0) {
            repeat(5) {
                world.addParticle(
                    ParticleTypes.CLOUD,
                    pos.x + (random.nextFloat() - 0.5f) * (width + 1) / 2.0,
                    pos.y + height / 2 + (random.nextFloat() - 0.5f) * height / 2.0,
                    pos.z + (random.nextFloat() - 0.5f) * (width + 1) / 2.0,
                    -velocity.x,
                    0.0,
                    -velocity.z
                )
            }
        }
    }
}