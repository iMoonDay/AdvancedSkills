package com.imoonday.advskills_re.entity

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.*
import com.imoonday.advskills_re.util.*
import net.minecraft.block.*
import net.minecraft.entity.*
import net.minecraft.entity.ai.pathing.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.passive.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.math.*
import net.minecraft.util.math.random.*
import net.minecraft.world.*
import kotlin.math.*

class SpecialTameHorseEntity(entityType: EntityType<out HorseEntity>, world: World) : HorseEntity(entityType, world) {

    constructor(world: World, owner: PlayerEntity) : this(ModEntities.SPECIAL_TAME_HORSE.get(), world) {
        bondWithPlayer(owner)
        saddle(SoundCategory.NEUTRAL)
        equipHorseArmor(owner, Items.DIAMOND_HORSE_ARMOR.defaultStack)
        setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f)
        updateSaddle()
        customName = Skills.EXCLUSIVE_MOUNT.message("customName", owner.displayName)
    }

    override fun method_48926(): EntityView = world

    public override fun putPlayerOnBack(player: PlayerEntity) {
        if (owner != player) {
            if (!world.isClient) player.sendMessage(Skills.EXCLUSIVE_MOUNT.message("notOwner"), true)
            return
        }
        super.putPlayerOnBack(player)
    }

    public override fun initAttributes(random: Random) {
        getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH)!!.baseValue =
            getChildHealthBonus { it }.toDouble()
        getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED)!!.baseValue =
            getChildMovementSpeedBonus { 1.0 }
        getAttributeInstance(EntityAttributes.HORSE_JUMP_STRENGTH)!!.baseValue =
            getChildJumpStrengthBonus { 1.0 }
    }

    override fun onDeath(damageSource: DamageSource) {
        if (!world.isClient) (owner as? ServerPlayerEntity)?.run {
            cooldown(summonSkill)
            val attacker = damageSource.attacker ?: damageSource.source
            sendMessage(
                if (attacker == null) {
                    Skills.EXCLUSIVE_MOUNT.message("dead", blockPos.x, blockPos.y, blockPos.z)
                } else {
                    Skills.EXCLUSIVE_MOUNT.message(
                        "killed",
                        blockPos.x,
                        blockPos.y,
                        blockPos.z,
                        attacker.displayName
                    )
                }
            )
        }
        super.onDeath(damageSource)
    }

    override fun drop(source: DamageSource) = Unit

    fun tryTeleport() {
        val blockPos = owner!!.blockPos
        for (i in 0..9) {
            val j = this.getRandomInt(-3, 3)
            val k = this.getRandomInt(-1, 1)
            val l = this.getRandomInt(-3, 3)
            val bl = this.tryTeleportTo(blockPos.x + j, blockPos.y + k, blockPos.z + l)
            if (!bl) continue
            return
        }
        refreshPositionAndAngles(owner!!.x, owner!!.y, owner!!.z, yaw, pitch)
    }

    private fun tryTeleportTo(x: Int, y: Int, z: Int): Boolean {
        if (abs(x.toDouble() - this.owner!!.x) < 2.0 && abs(z.toDouble() - this.owner!!.z) < 2.0) {
            return false
        }
        if (!this.canTeleportTo(BlockPos(x, y, z))) {
            return false
        }
        refreshPositionAndAngles(x.toDouble() + 0.5, y.toDouble(), z.toDouble() + 0.5, yaw, pitch)
        navigation.stop()
        return true
    }

    private fun canTeleportTo(pos: BlockPos): Boolean {
        val pathNodeType = LandPathNodeMaker.getLandNodeType(this.world, pos.mutableCopy())
        if (pathNodeType != PathNodeType.WALKABLE) {
            return false
        }
        val blockState = world.getBlockState(pos.down())
        if (blockState.block is LeavesBlock) {
            return false
        }
        val blockPos = pos.subtract(blockPos)
        return world.isSpaceEmpty(this, boundingBox.offset(blockPos))
    }

    private fun getRandomInt(min: Int, max: Int): Int {
        return random.nextInt(max - min + 1) + min
    }

    override fun openInventory(player: PlayerEntity) {
        if (!world.isClient) {
            player.sendMessage(
                Skills.EXCLUSIVE_MOUNT.message("inventory"),
                true
            )
        }
    }

    override fun tick() {
        if (!world.isClient) {
            owner?.let {
                val properties = it.properties
                if (!properties.containsUuid(ExclusiveMountSkill.NBT_HORSE_UUID)) {
                    properties.putUuid(ExclusiveMountSkill.NBT_HORSE_UUID, uuid)
                    it.syncProperties()
                } else if (properties.getUuid(ExclusiveMountSkill.NBT_HORSE_UUID) != uuid) {
                    discard()
                    return
                }
            }
        }
        super.tick()
    }

    companion object {

        private val summonSkill
            get() = Skills.EXCLUSIVE_MOUNT
    }
}