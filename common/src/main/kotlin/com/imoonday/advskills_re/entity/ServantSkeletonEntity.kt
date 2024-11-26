package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.goal.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.mob.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.nbt.*
import net.minecraft.server.*
import net.minecraft.sound.*
import net.minecraft.world.*
import java.util.*

class ServantSkeletonEntity(
    entityType: EntityType<out AbstractSkeletonEntity>,
    world: World,
) : AbstractSkeletonEntity(entityType, world), Servant {

    override var ownerUuid: UUID? = null

    constructor(world: World, owner: PlayerEntity) : this(ModEntities.SERVANT_SKELETON.get(), world) {
        ownerUuid = owner.uuid
        customName = Skills.UNDEAD_SUMMONING.message("customName", owner.displayName.string)
        equipStack(EquipmentSlot.MAINHAND, ItemStack(Items.BOW))
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
            ) { it.uuid != ownerUuid && (it as? PlayerEntity)?.run { SkillTriggerHandler.isTaunter(this) } == true })
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

    override fun getAmbientSound(): SoundEvent = SoundEvents.ENTITY_SKELETON_AMBIENT

    override fun getHurtSound(source: DamageSource?): SoundEvent = SoundEvents.ENTITY_SKELETON_HURT

    override fun getDeathSound(): SoundEvent = SoundEvents.ENTITY_SKELETON_DEATH

    override fun getStepSound(): SoundEvent = SoundEvents.ENTITY_SKELETON_STEP

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