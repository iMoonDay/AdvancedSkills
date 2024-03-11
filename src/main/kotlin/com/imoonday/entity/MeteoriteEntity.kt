package com.imoonday.entity

import com.imoonday.init.ModEntities
import com.imoonday.util.blockPosSet
import com.imoonday.util.id
import net.minecraft.client.model.*
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.*
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

class MeteoriteEntity(type: EntityType<*>, world: World) : Entity(type, world), Ownable {

    private var ownerUuid: UUID? = null
        get() = ownerEntity?.uuid ?: field
    var ownerEntity: Entity? = null
        set(value) {
            field = value
            ownerUuid = value?.uuid
        }
    var radius: Float
        get() = dataTracker.get(radiusData)
        set(value) {
            dataTracker.set(radiusData, max(value, 0.5f))
        }

    constructor(world: World, pos: Vec3d, radius: Float, owner: LivingEntity?) : this(ModEntities.METEORITE, world) {
        this.setPosition(pos)
        this.radius = radius
        this.ownerEntity = owner
    }

    override fun tick() {
        super.tick()
        val velocity = velocity
        val box = boundingBox
        if (!world.isClient) {
            val collideCount = box.blockPosSet
                .filter { !world.getBlockState(it).isAir && world.getBlockState(it).fluidState.isEmpty }
                .count { world.breakBlock(it, this.radius <= 50, this) }
            if (shouldExplode(collideCount)) {
                world.createExplosion(ownerEntity, x, box.minY, z, 2 + radius * 2, true, World.ExplosionSourceType.MOB)
                discard()
            } else {
                val hitResult = ProjectileUtil.getCollision(this) { canHit() }
                if (hitResult.type == HitResult.Type.ENTITY && hitResult is EntityHitResult) {
                    val entity = hitResult.entity
                    (entity as? LivingEntity)?.damage((ownerEntity ?: this).damageSources.inWall(), 2.0f)
                }
            }
        }
        this.setPosition(pos.add(velocity))
        addVelocity(0.0, -0.02, 0.0)
        ProjectileUtil.setRotationFromVelocity(this, 0.5f)
        world.addParticle(
            ParticleTypes.EXPLOSION_EMITTER,
            x, box.maxY, z,
            -velocity.x, -velocity.y, -velocity.z
        )
        if (this.age % 5 == 0) {
            this.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.0f)
        }
    }

    fun shouldExplode(collideCount: Int): Boolean = collideCount >= getExplosionCount()

    fun getExplosionCount(): Int = (boundingBox.xLength * boundingBox.zLength).toInt()

    override fun initDataTracker() = dataTracker.startTracking(radiusData, 0.5f)

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        if (nbt.containsUuid("Owner")) {
            ownerUuid = nbt.getUuid("Owner")
            ownerEntity = null
        }
        if (nbt.contains("Radius", NbtElement.FLOAT_TYPE.toInt())) {
            radius = nbt.getFloat("Radius")
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid)
        }
        nbt.putFloat("Radius", radius)
    }

    override fun getDimensions(pose: EntityPose): EntityDimensions =
        super.getDimensions(pose).scaled(radius * 2f)

    override fun calculateBoundingBox(): Box = super.calculateBoundingBox().expand(radius.toDouble())

    override fun getOwner(): Entity? {
        if (ownerEntity != null && !ownerEntity!!.isRemoved) {
            return ownerEntity
        }
        if (ownerUuid != null && world is ServerWorld) {
            ownerEntity = (world as ServerWorld).getEntity(ownerUuid)
            return ownerEntity
        }
        return null
    }

    override fun setVelocityClient(x: Double, y: Double, z: Double) {
        this.setVelocity(x, y, z)
        if (prevPitch == 0.0f && prevYaw == 0.0f) {
            val d = sqrt(x * x + z * z)
            pitch = (MathHelper.atan2(y, d) * 57.2957763671875).toFloat()
            yaw = (MathHelper.atan2(x, z) * 57.2957763671875).toFloat()
            prevPitch = pitch
            prevYaw = yaw
            refreshPositionAndAngles(this.x, this.y, this.z, yaw, pitch)
        }
    }

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> =
        EntitySpawnS2CPacket(this, getOwner()?.id ?: 0)

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        val entity = world.getEntityById(packet.entityData)
        if (entity != null) {
            ownerEntity = entity
        }
    }

    class Renderer(ctx: EntityRendererFactory.Context) : EntityRenderer<MeteoriteEntity>(ctx) {

        private val main: ModelPart = ctx.getPart(ModEntities.METEORITE_MODEL_LAYER)

        override fun render(
            entity: MeteoriteEntity,
            yaw: Float,
            tickDelta: Float,
            matrices: MatrixStack,
            vertexConsumers: VertexConsumerProvider,
            light: Int,
        ) {
            matrices.push()
            val scale: Float = (entity.radius + 0.5f) * 2f
            matrices.scale(scale, scale, scale)
            matrices.translate(0f, -(0.75f + (entity.radius - 0.5f) / 2f / scale), 0f)
            val consumer = vertexConsumers.getBuffer(LAYER)
            main.render(matrices, consumer, light, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 1.0f)
            matrices.pop()
        }

        override fun getTexture(entity: MeteoriteEntity): Identifier = TEXTURE

        companion object {

            private val TEXTURE: Identifier = id("textures/entity/meteorite.png")
            private val LAYER: RenderLayer = RenderLayer.getEntityCutoutNoCull(TEXTURE)
            val texturedModelData: TexturedModelData
                get() {
                    val modelData = ModelData()
                    val modelPartData = modelData.root
                    modelPartData.addChild(
                        "main",
                        ModelPartBuilder.create().uv(86, 32)
                            .cuboid(-4.0f, -16.0f, -4.0f, 8.0f, 16.0f, 8.0f, Dilation(0.0f)).uv(50, 47)
                            .cuboid(-5.0f, -15.0f, -5.0f, 10.0f, 14.0f, 10.0f, Dilation(0.0f)).uv(0, 40)
                            .cuboid(-6.0f, -14.0f, -6.0f, 12.0f, 12.0f, 12.0f, Dilation(0.0f)).uv(0, 0)
                            .cuboid(-8.0f, -12.0f, -4.0f, 16.0f, 8.0f, 8.0f, Dilation(0.0f)).uv(0, 16)
                            .cuboid(-7.0f, -13.0f, -5.0f, 14.0f, 10.0f, 10.0f, Dilation(0.0f)).uv(0, 64)
                            .cuboid(-5.0f, -13.0f, -7.0f, 10.0f, 10.0f, 14.0f, Dilation(0.0f)).uv(48, 74)
                            .cuboid(-4.0f, -12.0f, -8.0f, 8.0f, 8.0f, 16.0f, Dilation(0.0f)),
                        ModelTransform.pivot(0.0f, 24.0f, 0.0f)
                    )
                    return TexturedModelData.of(modelData, 128, 128)
                }
        }
    }

    companion object {

        private val radiusData: TrackedData<Float> =
            DataTracker.registerData(MeteoriteEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }
}