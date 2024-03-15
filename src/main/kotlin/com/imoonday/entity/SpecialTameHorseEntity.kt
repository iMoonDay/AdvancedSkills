package com.imoonday.entity

import com.imoonday.util.startCooling
import com.imoonday.component.status
import com.imoonday.util.stopCooling
import com.imoonday.init.ModComponents
import com.imoonday.init.ModEntities
import com.imoonday.init.ModSkills
import com.imoonday.util.translateSkill
import net.minecraft.block.LeavesBlock
import net.minecraft.entity.EntityType
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.ai.pathing.LandPathNodeMaker
import net.minecraft.entity.ai.pathing.PathNodeType
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.passive.HorseEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.random.Random
import net.minecraft.world.EntityView
import net.minecraft.world.World
import kotlin.math.abs

class SpecialTameHorseEntity(entityType: EntityType<out HorseEntity>, world: World) : HorseEntity(entityType, world) {

    private val summonSkill
        get() = ModSkills.EXCLUSIVE_MOUNT

    constructor(world: World, owner: PlayerEntity) : this(ModEntities.SPECIAL_TAME_HORSE, world) {
        bondWithPlayer(owner)
        saddle(SoundCategory.NEUTRAL)
        equipHorseArmor(owner, Items.DIAMOND_HORSE_ARMOR.defaultStack)
        setEquipmentDropChance(EquipmentSlot.CHEST, 0.0f)
        updateSaddle()
        customName = translateSkill("exclusive_mount", "customName", owner.displayName.string)
    }

    override fun method_48926(): EntityView = world

    public override fun putPlayerOnBack(player: PlayerEntity) {
        if (owner != player) {
            if (!world.isClient) player.sendMessage(translateSkill("exclusive_mount", "notOwner"), true)
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
            stopCooling(summonSkill)
            startCooling(summonSkill)
            val attacker = damageSource.attacker ?: damageSource.source
            sendMessage(
                if (attacker == null) {
                    translateSkill("exclusive_mount", "dead", blockPos.x, blockPos.y, blockPos.z)
                } else {
                    translateSkill(
                        "exclusive_mount",
                        "killed",
                        blockPos.x,
                        blockPos.y,
                        blockPos.z,
                        attacker.displayName.string
                    )
                }
            )
        }
        super.onDeath(damageSource)
    }

    override fun drop(source: DamageSource) {
    }

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
        if (!world.isClient) player.sendMessage(
            translateSkill("exclusive_mount", "inventory"),
            true
        )
    }

    override fun tick() {
        if (!world.isClient) {
            owner?.let {
                if (!it.status.containsUuid("horseUuid")) {
                    it.status.putUuid("horseUuid", uuid)
                    ModComponents.STATUS.sync(it)
                } else if (it.status.getUuid("horseUuid") != uuid) {
                    discard()
                    return
                }
            }
        }
        super.tick()
    }
}