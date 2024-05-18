package com.imoonday.entity

import com.imoonday.init.ModEntities
import net.minecraft.entity.*
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.data.DataTracker
import net.minecraft.entity.data.TrackedData
import net.minecraft.entity.data.TrackedDataHandlerRegistry
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.listener.ClientPlayPacketListener
import net.minecraft.network.packet.Packet
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Arm
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MagnetEntity(entityType: EntityType<out MagnetEntity>, world: World) : LivingEntity(entityType, world) {

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

    constructor(world: World, pos: Vec3d, owner: Entity) : this(ModEntities.MAGNET, world) {
        refreshPositionAndAngles(pos.x, pos.y, pos.z, yaw, pitch)
        this.owner = owner
    }

    override fun initDataTracker() {
        super.initDataTracker()
        dataTracker.startTracking(RADIUS, 3.0f)
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
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        if (ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid)
        }
        nbt.putFloat("Radius", radius)
    }

    override fun tick() {
        super.tick()
        world.getOtherEntities(
            this,
            boundingBox.expand(radius.toDouble(), 1.5, radius.toDouble())
        ) {
            it.isLiving && it.isAlive && !it.isSpectator && !it.isSneaking && it != owner && (it !is Ownable || it.owner != it)
        }.map { it as LivingEntity }.forEach {
            if (age % 20 == 0 || !it.hasStatusEffect(StatusEffects.SLOWNESS)) it.addStatusEffect(
                StatusEffectInstance(
                    StatusEffects.SLOWNESS,
                    21,
                    2
                )
            )
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

    override fun createSpawnPacket(): Packet<ClientPlayPacketListener> = EntitySpawnS2CPacket(this, owner?.id ?: 0)

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        owner = world.getEntityById(packet.entityData)
    }

    companion object {

        val RADIUS: TrackedData<Float> =
            DataTracker.registerData(MagnetEntity::class.java, TrackedDataHandlerRegistry.FLOAT)

        fun createLivingAttributes(): DefaultAttributeContainer.Builder =
            DefaultAttributeContainer.builder()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 10.0)
                .add(EntityAttributes.GENERIC_KNOCKBACK_RESISTANCE, 1.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.0)
                .add(EntityAttributes.GENERIC_ARMOR)
                .add(EntityAttributes.GENERIC_ARMOR_TOUGHNESS)
    }
}