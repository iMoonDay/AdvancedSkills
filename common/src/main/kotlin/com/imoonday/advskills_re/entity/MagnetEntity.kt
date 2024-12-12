package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import dev.architectury.extensions.network.*
import dev.architectury.networking.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.data.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.network.*
import net.minecraft.network.listener.*
import net.minecraft.network.packet.*
import net.minecraft.particle.*
import net.minecraft.registry.tag.*
import net.minecraft.server.world.*
import net.minecraft.util.*
import net.minecraft.util.math.*
import net.minecraft.world.*
import java.util.*
import kotlin.math.*

class MagnetEntity(entityType: EntityType<out MagnetEntity>, world: World) : LivingEntity(entityType, world),
    EntitySpawnExtension {

    init {
        noClip = true
        setNoGravity(true)
    }

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
    var radius: Float
        get() = dataTracker.get(RADIUS)
        set(value) = dataTracker.set(RADIUS, value)
    var maxAge: Int
        get() = dataTracker.get(MAX_AGE)
        set(value) = dataTracker.set(MAX_AGE, value)

    constructor(world: World, pos: Vec3d, owner: Entity) : this(ModEntities.MAGNET.get(), world) {
        refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch)
        this.owner = owner
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(RADIUS, 3.0f)
        dataTracker.startTracking(MAX_AGE, 20 * 60 * 5)
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        if (nbt.containsUuid("Owner")) {
            this.ownerUuid = nbt.getUuid("Owner")
            this.owner = null
        }
        if (nbt.contains("Radius")) {
            this.radius = nbt.getFloat("Radius")
        }
        if (nbt.contains("MaxAge")) {
            this.maxAge = nbt.getInt("MaxAge")
        }
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid)
        }
        nbt.putFloat("Radius", radius)
        nbt.putInt("MaxAge", maxAge)
    }

    override fun tick() {
        super.tick()
        if (!world.isClient && age > maxAge) {
            discard()
            return
        }
        world.getOtherEntities(
            this,
            boundingBox.expand(radius.toDouble(), 1.5, radius.toDouble())
        ) {
            it.isLiving && it.isAlive && !it.isSpectator && !it.isSneaking && it != owner && it.uuid != ownerUuid && (it !is Ownable || it.owner != it)
        }.map { it as LivingEntity }.forEach {
            if (age % 20 == 0 || !it.hasStatusEffect(StatusEffects.SLOWNESS)) {
                it.addStatusEffect(
                    StatusEffectInstance(StatusEffects.SLOWNESS, 21, 2)
                )
            }
            it.addVelocity(pos.subtract(it.pos).normalize().multiply(0.025))
            it.velocityDirty = true
        }
        if (age % 5 == 0) {
            addParticles(ParticleTypes.GLOW, radius + width / 2.0, 36)
        }
    }

    private fun addParticles(type: ParticleEffect, radius: Double, quantity: Int) {
        for (i in 0 until quantity) {
            val theta = 2 * PI * i / quantity.toDouble()
            val particleX = x + radius * cos(theta)
            val particleZ = z + radius * sin(theta)
            world.addParticle(type, particleX, y + height / 2, particleZ, 0.0, 0.0, 0.0)
        }
    }

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean =
        (isRemoved || damageSource.source !is LivingEntity && damageSource.attacker !is LivingEntity || damageSource.source == owner || damageSource.attacker == owner)
            && !damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)

    override fun canMoveVoluntarily(): Boolean = false

    override fun isPushable(): Boolean = false

    override fun canBreatheInWater(): Boolean = true

    override fun isPushedByFluids(): Boolean = false

    override fun collidesWith(other: Entity): Boolean = false

    override fun getArmorItems(): MutableIterable<ItemStack> = mutableListOf()

    override fun equipStack(slot: EquipmentSlot, stack: ItemStack) = Unit

    override fun getEquippedStack(slot: EquipmentSlot): ItemStack = ItemStack.EMPTY

    override fun getMainArm(): Arm = Arm.RIGHT

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> = NetworkManager.createAddEntityPacket(this)

    override fun interact(player: PlayerEntity, hand: Hand): ActionResult {
        if (player.uuid == ownerUuid && player.isSneaking) {
            this.discard()
            return ActionResult.SUCCESS
        }
        return super.interact(player, hand)
    }

    companion object {

        val RADIUS: TrackedData<Float> =
            DataTracker.registerData(MagnetEntity::class.java, TrackedDataHandlerRegistry.FLOAT)

        val MAX_AGE: TrackedData<Int> =
            DataTracker.registerData(MagnetEntity::class.java, TrackedDataHandlerRegistry.INTEGER)

        fun createLivingAttributes(): DefaultAttributeContainer.Builder =
            DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
    }

    override fun saveAdditionalSpawnData(buf: PacketByteBuf) {
        buf.writeInt(owner?.id ?: 0)
        buf.writeUuid(ownerUuid)
    }

    override fun loadAdditionalSpawnData(buf: PacketByteBuf) {
        val entityId = buf.readInt()
        ownerUuid = buf.readUuid()
        owner = world.getEntityById(entityId)
    }
}