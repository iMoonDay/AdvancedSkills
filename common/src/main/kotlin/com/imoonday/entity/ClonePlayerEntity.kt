package com.imoonday.entity

import com.imoonday.init.ModEntities
import com.imoonday.util.client
import fi.dy.masa.malilib.util.NBTUtils
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.LivingEntityRenderer
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.PlayerEntityModel
import net.minecraft.client.util.DefaultSkinHelper
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Arm
import net.minecraft.util.Identifier
import net.minecraft.util.Uuids
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

class ClonePlayerEntity(entityType: EntityType<out ClonePlayerEntity>, world: World) : LivingEntity(entityType, world) {

    var playerUUID: UUID
        get() = dataTracker.get(Companion.uuid).orElse(Uuids.getOfflinePlayerUuid(customName?.string ?: "Steve"))
        set(value) = dataTracker.set(Companion.uuid, Optional.of(value))
    var moveVelocity: Vec3d
        get() = NBTUtils.readVec3d(dataTracker.get(data)) ?: Vec3d.ZERO
        set(value) = dataTracker.set(data, dataTracker.get(data).apply {
            NBTUtils.writeVec3dToTag(value, this)
        })
    var moveTime: Int
        get() = dataTracker.get(data).getInt("time")
        set(value) = dataTracker.set(data, dataTracker.get(data).apply {
            putInt("time", value)
        })

    constructor(world: World, player: PlayerEntity) : this(ModEntities.CLONE_PLAYER, world) {
        playerUUID = player.uuid
        refreshPositionAndAngles(player.x, player.y, player.z, player.yaw, player.pitch)
        headYaw = player.headYaw
        customName = player.displayName
    }

    override fun tick() {
        if (moveTime > 0) {
            moveTime--
            velocity = moveVelocity.withAxis(Direction.Axis.Y, velocity.y)
            if (horizontalCollision) moveTime = 0
        }
        if (jumping && moveTime <= 0) jumping = false
        super.tick()
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(Companion.uuid, Optional.of(Uuids.getOfflinePlayerUuid("Steve")))
        dataTracker.startTracking(data, NbtCompound())
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.contains("PlayerUuid")) {
            playerUUID = nbt.getUuid("PlayerUuid")
        }
        if (nbt.contains("Data")) {
            dataTracker.set(data, nbt.getCompound("Data"))
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        nbt.putUuid("PlayerUuid", playerUUID)
        nbt.put("Data", dataTracker.get(data))
    }

    override fun getArmorItems(): MutableIterable<ItemStack> = mutableListOf()

    override fun equipStack(slot: EquipmentSlot, stack: ItemStack) = Unit

    override fun getEquippedStack(slot: EquipmentSlot?): ItemStack = ItemStack.EMPTY

    override fun getMainArm(): Arm = Arm.RIGHT

    override fun isPlayer(): Boolean = true

    class Renderer(
        ctx: EntityRendererFactory.Context,
    ) : LivingEntityRenderer<ClonePlayerEntity, PlayerEntityModel<ClonePlayerEntity>>(
        ctx,
        PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f
    ) {

        override fun getTexture(entity: ClonePlayerEntity): Identifier =
            client!!.networkHandler?.getPlayerListEntry(entity.playerUUID)?.skinTexture
                ?: DefaultSkinHelper.getTexture(entity.playerUUID)
    }

    companion object {

        val uuid: TrackedData<Optional<UUID>> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
        val data: TrackedData<NbtCompound> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.NBT_COMPOUND)
    }
}