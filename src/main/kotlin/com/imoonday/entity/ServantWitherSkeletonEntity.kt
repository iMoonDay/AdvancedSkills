package com.imoonday.entity

import com.imoonday.util.isUsing
import com.imoonday.init.ModEntities
import com.imoonday.init.ModSkills
import com.imoonday.util.translateSkill
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.ai.goal.ActiveTargetGoal
import net.minecraft.entity.ai.goal.LookAroundGoal
import net.minecraft.entity.ai.goal.LookAtEntityGoal
import net.minecraft.entity.ai.goal.WanderAroundFarGoal
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.DefaultAttributeContainer
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.entity.mob.HostileEntity
import net.minecraft.entity.mob.WitherSkeletonEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.nbt.NbtCompound
import net.minecraft.server.ServerConfigHandler
import net.minecraft.world.World
import java.util.*

class ServantWitherSkeletonEntity(
    entityType: EntityType<out WitherSkeletonEntity>,
    world: World,
) : WitherSkeletonEntity(entityType, world), Servant {

    override var ownerUuid: UUID? = null

    init {
        setPathfindingPenalty(PathNodeType.LAVA, 8.0f)
    }

    constructor(world: World, owner: PlayerEntity) : this(ModEntities.SERVANT_WITHER_SKELETON, world) {
        ownerUuid = owner.uuid
        customName = translateSkill("undead_summoning", "customName", owner.displayName.string)
        equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.STONE_SWORD))
        refreshPositionAndAngles(owner.x, owner.y, owner.z, owner.yaw, owner.pitch)
    }

    override fun initGoals() {
        goalSelector.add(2, WanderAroundFarGoal(this, 1.0))
        goalSelector.add(3, LookAtEntityGoal(this, PlayerEntity::class.java, 8.0f))
        goalSelector.add(3, LookAroundGoal(this))
        targetSelector.add(
            0,
            ActiveTargetGoal(
                this,
                PlayerEntity::class.java,
                true
            ) { it.uuid != ownerUuid && (it as PlayerEntity).isUsing(ModSkills.TAUNT) })
        targetSelector.add(
            1,
            ActiveTargetGoal(this, LivingEntity::class.java, true) { it is Servant && it.ownerUuid != this.ownerUuid })
        targetSelector.add(2, ActiveTargetGoal(this, PlayerEntity::class.java, true) { it.uuid != ownerUuid })
        targetSelector.add(
            3,
            ActiveTargetGoal(this, HostileEntity::class.java, true) { it !is Servant })
    }

    override fun isAffectedByDaylight(): Boolean = false

    override fun isShaking(): Boolean = false

    override fun canFreeze(): Boolean = false

    override fun drop(source: DamageSource) {
    }

    override fun getOwner(): Entity? = world.getPlayerByUuid(ownerUuid)

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        super.writeCustomDataToNbt(nbt)
        if (this.ownerUuid != null) {
            nbt.putUuid("Owner", ownerUuid)
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        super.readCustomDataFromNbt(nbt)
        val uuid: UUID? = if (nbt.containsUuid("Owner")) {
            nbt.getUuid("Owner")
        } else {
            ServerConfigHandler.getPlayerUuidByName(server, nbt.getString("Owner"))
        }
        if (uuid != null) {
            ownerUuid = uuid
        }
    }

    override fun tick() {
        if (!world.isClient && (ownerUuid == null || age > 20 * 60)) {
            kill()
        }
        super.tick()
    }

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean {
        if (damageSource.isOf(DamageTypes.FALL)) return true
        val attacker = damageSource.attacker
        val source = damageSource.source
        if (attacker == null && source == null) return false
        return super.isInvulnerableTo(damageSource) || attacker?.uuid == ownerUuid || source?.uuid == ownerUuid || attacker is Servant && attacker.ownerUuid == ownerUuid || source is Servant && source.ownerUuid == ownerUuid
    }

    companion object {

        fun createAttributes(): DefaultAttributeContainer.Builder {
            return createHostileAttributes().add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.5)
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 1.0)
        }
    }
}