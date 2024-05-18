package com.imoonday.entity

import com.imoonday.init.ModEntities
import com.imoonday.util.minus
import com.imoonday.util.times
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class HookEntity(type: EntityType<out HookEntity>, world: World) : Entity(type, world) {

    var owner: Entity? = null
        get() {
            if (field != null && !field!!.isRemoved) {
                return field
            }
            if (ownerUuid != null && world is ServerWorld) {
                field = (world as ServerWorld).getEntity(ownerUuid)
                return field
            }
            return null
        }
        set(value) {
            if (value != null) {
                field = value
                ownerUuid = value.uuid
            }
        }
    var ownerUuid: UUID? = null
    var target: LivingEntity? = null
        get() {
            if (field != null && !field!!.isRemoved) {
                return field
            }
            if (targetUuid != null && world is ServerWorld) {
                field = (world as ServerWorld).getEntity(targetUuid) as? LivingEntity
                return field
            }
            return null
        }
        set(value) {
            if (value != null) {
                field = value
                targetUuid = value.uuid
            }
        }
    var targetUuid: UUID?
        get() = dataTracker.get(TARGET_UUID).orElse(null)
        set(value) {
            dataTracker.set(TARGET_UUID, Optional.ofNullable(value))
        }
    var life: Int = 20 * 5

    constructor(world: World, owner: Entity) : this(ModEntities.HOOK, world) {
        this.owner = owner
    }

    override fun initDataTracker() {
        dataTracker.startTracking(TARGET_UUID, Optional.empty())
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner")
            this.owner = null
        }
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target")
            this.target = null
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid)
        }
        if (targetUuid != null) {
            nbt.putUuid("Target", targetUuid)
        }
    }

    override fun tick() {
        super.tick()
        if (owner == null || owner!!.isRemoved || age >= life) {
            if (!world.isClient) discard()
            return
        }
        if (target == null) move(MovementType.SELF, velocity)
        if (isOnGround) {
            if (!world.isClient) discard()
            return
        }
        ProjectileUtil.setRotationFromVelocity(this, 0.5f)
        if (target == null) {
            world.getOtherEntities(this, boundingBox.expand(1.0)) {
                it.isLiving && it.isAlive && !it.isSpectator && it != owner
            }.firstOrNull()?.let {
                target = it as? LivingEntity
                targetUuid = target?.uuid
            }
        }
        target?.let {
            it.addVelocity((owner!!.pos - it.pos).normalize() * 0.15)
            setPos(it.x, it.y + it.height / 2, it.z)
            if (!world.isClient && ((age % 20 == 0 || it.isPlayer && it.isSneaking) && random.nextFloat() < age / life * 0.75 || it.distanceTo(
                    owner!!
                ) <= 2)
            ) discard()
        }
        if (target == null) velocity = velocity.multiply(0.98).subtract(0.0, 0.01, 0.0) else Vec3d.ZERO
    }

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> = EntitySpawnS2CPacket(this, owner?.id ?: 0)

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        owner = world.getEntityById(packet.entityData)?.also {
            ownerUuid = it.uuid
        }
    }

    override fun shouldRender(cameraX: Double, cameraY: Double, cameraZ: Double): Boolean = true

    companion object {

        val TARGET_UUID: TrackedData<Optional<UUID>> =
            DataTracker.registerData(HookEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
    }
}