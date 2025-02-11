package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.data.*
import net.minecraft.entity.mob.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.util.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.util.*

class ClonePlayerEntity(entityType: EntityType<out ClonePlayerEntity>, world: World) :
    PathAwareEntity(entityType, world) {

    var playerUUID: UUID
        get() = dataTracker.get(UUID_DATA).orElse(Uuids.getOfflinePlayerUuid(customName?.string ?: "Steve"))
        set(value) = dataTracker.set(UUID_DATA, Optional.of(value))
    var moveVelocity: Vec3d
        get() = NbtUtils.readVec3d(dataTracker.get(NBT_DATA)) ?: Vec3d.ZERO
        set(value) = dataTracker.set(NBT_DATA, dataTracker.get(NBT_DATA).apply {
            NbtUtils.writeVec3dToTag(value, this)
        })
    var moveTime: Int
        get() = dataTracker.get(NBT_DATA).getInt("time")
        set(value) = dataTracker.set(NBT_DATA, dataTracker.get(NBT_DATA).apply {
            putInt("time", value)
        })
    var aggressive: Boolean
        get() = dataTracker.get(AGGRESSIVE_DATA)
        set(value) = dataTracker.set(AGGRESSIVE_DATA, value)
    private var goalAdded: Boolean = false

    constructor(world: World, player: PlayerEntity, aggressive: Boolean = false) : this(
        ModEntities.CLONE_PLAYER.get(), world
    ) {
        this.aggressive = aggressive
        playerUUID = player.uuid
        refreshPositionAndAngles(player.x, player.y, player.z, player.yaw, player.pitch)
        headYaw = player.headYaw
        customName = player.displayName
        health = player.health
        val baseSpeed = attributes.getBaseValue(EntityAttributes.GENERIC_MOVEMENT_SPEED)
        attributes.setFrom(player.attributes)
        attributes.getCustomInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)?.baseValue = baseSpeed
        EquipmentSlot.entries.forEach {
            val equippedStack = player.getEquippedStack(it)
            if (!equippedStack.isEmpty) {
                equipStack(it, equippedStack.copy())
            }
        }
        addCustomGoals()
    }

    override fun tick() {
        if (!world.isClient && age > 5 * 20 * 60) {
            discard()
            return
        }
        if (moveTime > 0) {
            moveTime--
            velocity = moveVelocity.withAxis(Direction.Axis.Y, velocity.y)
            if (horizontalCollision) moveTime = 0
            if (jumping) {
                if (moveTime <= 0) {
                    jumping = false
                } else {
                    jumpControl.setActive()
                }
            }
        }
        addCustomGoals()

        super.tick()
    }

    override fun tickMovement() {
        this.tickHandSwing()
        super.tickMovement()
    }

    private fun addCustomGoals() {
        if (moveTime <= 0 && !goalAdded) {
            goalAdded = true
            goalSelector.add(0, SwimGoal(this))
            goalSelector.add(1, MeleeAttackGoal(this, 1.0, false))
            goalSelector.add(2, EscapeDangerGoal(this, 1.3))
            goalSelector.add(3, WanderAroundFarGoal(this, 1.0))
            goalSelector.add(4, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
            goalSelector.add(5, LookAroundGoal(this))
            goalSelector.add(6, WanderAroundGoal(this, 1.3))
            targetSelector.add(0, ActiveTargetGoal(this, HostileEntity::class.java, true) { aggressive })
        }
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(UUID_DATA, Optional.of(Uuids.getOfflinePlayerUuid("Steve")))
        dataTracker.startTracking(NBT_DATA, NbtCompound())
        dataTracker.startTracking(AGGRESSIVE_DATA, false)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains("PlayerUuid")) {
            playerUUID = nbt.getUuid("PlayerUuid")
        }
        if (nbt.contains("Data")) {
            dataTracker.set(NBT_DATA, nbt.getCompound("Data"))
        }
        if (nbt.contains("Aggressive")) {
            aggressive = nbt.getBoolean("Aggressive")
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putUuid("PlayerUuid", playerUUID)
        nbt.put("Data", dataTracker.get(NBT_DATA))
        nbt.putBoolean("Aggressive", aggressive)
    }

    override fun shouldRenderName(): Boolean = true

    override fun drop(source: DamageSource) = Unit

    companion object {

        @JvmStatic
        val UUID_DATA: TrackedData<Optional<UUID>> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)

        @JvmStatic
        val NBT_DATA: TrackedData<NbtCompound> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.NBT_COMPOUND)

        @JvmStatic
        val AGGRESSIVE_DATA: TrackedData<Boolean> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.BOOLEAN)

        @JvmStatic
        fun createAttributes(): DefaultAttributeContainer.Builder =
            HostileEntity.createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
    }
}