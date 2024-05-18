package com.imoonday.entity

import com.imoonday.init.ModEntities
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.MovementType
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.world.World
import java.util.*

class EnchantedSwordEntity(type: EntityType<out EnchantedSwordEntity>, world: World) : ProjectileEntity(type, world) {

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
        get() = dataTracker.get(HookEntity.TARGET_UUID).orElse(null)
        set(value) {
            dataTracker.set(HookEntity.TARGET_UUID, Optional.ofNullable(value))
        }

    init {
        noClip = true
    }

    constructor(world: World, owner: Entity, target: LivingEntity) : this(ModEntities.ENCHANTED_SWORD, world) {
        this.owner = owner
        this.target = target
    }

    override fun initDataTracker() {
        dataTracker.startTracking(TARGET_UUID, Optional.empty())
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.containsUuid("Target")) {
            this.targetUuid = nbt.getUuid("Target")
            this.target = null
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        if (this.targetUuid != null) {
            nbt.putUuid("Target", this.targetUuid)
        }
    }

    override fun tick() {
        super.tick()
        if (!world.isClient && target == null) {
            discard()
            return
        }
        move(MovementType.SELF, velocity)
        target?.let {
            if (it.isDead || !it.isAlive || it.isRemoved || it.isInvisible || it.isInvulnerable || it.distanceTo(
                    owner ?: this
                ) > 64
            ) {
                discard()
                return
            }
            if (boundingBox.intersects(it.boundingBox)) {
                if (!world.isClient) it.damage(damageSources.mobProjectile(this, owner as? LivingEntity), 4f)
                kill()
                return
            }
            velocity = it.pos.add(0.0, it.height / 2.0, 0.0).subtract(pos).normalize().multiply(0.5)
            velocityDirty = true
            lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, it.eyePos)
        }
    }

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> =
        EntitySpawnS2CPacket(this, owner?.id ?: 0)

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        owner = world.getEntityById(packet.entityData)
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        kill()
        return true
    }

    override fun onRemoved() {
        super.onRemoved()
        world.playSound(null, blockPos, SoundEvents.ENTITY_ITEM_BREAK, SoundCategory.VOICE)
    }

    companion object {

        val TARGET_UUID: TrackedData<Optional<UUID>> =
            DataTracker.registerData(EnchantedSwordEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
    }
}