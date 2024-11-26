package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.data.*
import net.minecraft.entity.projectile.*
import net.minecraft.nbt.*
import net.minecraft.network.listener.*
import net.minecraft.network.packet.*
import net.minecraft.network.packet.s2c.play.*
import net.minecraft.particle.*
import net.minecraft.server.world.*
import net.minecraft.sound.*
import net.minecraft.util.hit.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.util.*
import kotlin.math.*

class MeteoriteEntity(type: EntityType<out MeteoriteEntity>, world: World) : Entity(type, world), Ownable {

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

    constructor(world: World, pos: Vec3d, radius: Float, owner: LivingEntity?) : this(
        ModEntities.METEORITE.get(),
        world
    ) {
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

    companion object {

        private val radiusData: TrackedData<Float> =
            DataTracker.registerData(MeteoriteEntity::class.java, TrackedDataHandlerRegistry.FLOAT)
    }
}