package com.imoonday.entity

import com.imoonday.init.*
import com.imoonday.util.*
import net.minecraft.client.render.entity.*
import net.minecraft.client.render.entity.feature.*
import net.minecraft.client.render.entity.model.*
import net.minecraft.client.util.*
import net.minecraft.client.util.math.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.goal.*
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
        get() = dataTracker.get(Companion.uuid).orElse(Uuids.getOfflinePlayerUuid(customName?.string ?: "Steve"))
        set(value) = dataTracker.set(Companion.uuid, Optional.of(value))
    var moveVelocity: Vec3d
        get() = NbtUtils.readVec3d(dataTracker.get(data)) ?: Vec3d.ZERO
        set(value) = dataTracker.set(data, dataTracker.get(data).apply {
            NbtUtils.writeVec3dToTag(value, this)
        })
    var moveTime: Int
        get() = dataTracker.get(data).getInt("time")
        set(value) = dataTracker.set(data, dataTracker.get(data).apply {
            putInt("time", value)
        })

    constructor(world: World, player: PlayerEntity) : this(ModEntities.CLONE_PLAYER.get(), world) {
        playerUUID = player.uuid
        refreshPositionAndAngles(player.x, player.y, player.z, player.yaw, player.pitch)
        headYaw = player.headYaw
        customName = player.displayName
        health = player.health
        attributes.setFrom(player.attributes)
        EquipmentSlot.entries.forEach {
            val equippedStack = player.getEquippedStack(it)
            if (!equippedStack.isEmpty) {
                equipStack(it, equippedStack.copy())
            }
        }
    }

    override fun tick() {
        if (age > 20 * 60 * 5) {
            kill()
            return
        }
        if (moveTime-- > 0) {
            velocity = moveVelocity.withAxis(Direction.Axis.Y, velocity.y)
            if (horizontalCollision) moveTime = 0
            if (jumping) {
                if (moveTime <= 0) {
                    jumping = false
                } else {
                    jumpControl.setActive()
                }
            }
            if (moveTime <= 0) {
                goalSelector.add(0, SwimGoal(this))
                goalSelector.add(1, EscapeDangerGoal(this, 3.2))
                goalSelector.add(2, WanderAroundFarGoal(this, 2.5))
                goalSelector.add(3, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
                goalSelector.add(4, LookAroundGoal(this))
                goalSelector.add(5, WanderAroundGoal(this, 3.2))
            }
        }
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

    override fun isPlayer(): Boolean = true

    override fun shouldRenderName(): Boolean = true

    override fun drop(source: DamageSource) = Unit

    class Renderer(
        ctx: EntityRendererFactory.Context,
    ) : BipedEntityRenderer<ClonePlayerEntity, PlayerEntityModel<ClonePlayerEntity>>(
        ctx,
        PlayerEntityModel(ctx.getPart(EntityModelLayers.PLAYER), false), 0.5f
    ) {

        private val defaultModel = PlayerEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER), false)
        private val slimModel = PlayerEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM), false)
        private val defaultInnerArmor =
            ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_INNER_ARMOR))
        private val slimInnerArmor =
            ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM_INNER_ARMOR))
        private val defaultOuterArmor =
            ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_OUTER_ARMOR))
        private val slimOuterArmor =
            ArmorEntityModel<ClonePlayerEntity>(ctx.getPart(EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR))
        private val armorFeatureRenderer = ArmorFeatureRenderer(
            this,
            defaultInnerArmor,
            defaultOuterArmor,
            ctx.modelManager
        )

        init {
            this.addFeature(armorFeatureRenderer)
            this.addFeature(StuckArrowsFeatureRenderer(ctx, this))
            this.addFeature(TridentRiptideFeatureRenderer(this, ctx.modelLoader))
            this.addFeature(StuckStingersFeatureRenderer(this))
        }

        override fun getTexture(entity: ClonePlayerEntity): Identifier {
            return client?.networkHandler?.getPlayerListEntry(entity.playerUUID)?.run {
                val slim = this.model == "slim"
                super.model = if (slim) slimModel else defaultModel
                armorFeatureRenderer.innerModel = if (slim) slimInnerArmor else defaultInnerArmor
                armorFeatureRenderer.outerModel = if (slim) slimOuterArmor else defaultOuterArmor
                skinTexture
            } ?: run {
                val model = DefaultSkinHelper.getModel(entity.playerUUID)
                val slim = model == "slim"
                super.model = if (slim) slimModel else defaultModel
                armorFeatureRenderer.innerModel = if (slim) slimInnerArmor else defaultInnerArmor
                armorFeatureRenderer.outerModel = if (slim) slimOuterArmor else defaultOuterArmor
                DefaultSkinHelper.getTexture(entity.playerUUID)
            }
        }

        override fun getPositionOffset(entity: ClonePlayerEntity, f: Float): Vec3d =
            if (entity.isInSneakingPose) Vec3d(0.0, -0.125, 0.0) else super.getPositionOffset(entity, f)

        override fun scale(entity: ClonePlayerEntity, matrices: MatrixStack, amount: Float) {
            val g = 0.9375f
            matrices.scale(g, g, g)
        }
    }

    companion object {

        val uuid: TrackedData<Optional<UUID>> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.OPTIONAL_UUID)
        val data: TrackedData<NbtCompound> =
            DataTracker.registerData(ClonePlayerEntity::class.java, TrackedDataHandlerRegistry.NBT_COMPOUND)
    }
}