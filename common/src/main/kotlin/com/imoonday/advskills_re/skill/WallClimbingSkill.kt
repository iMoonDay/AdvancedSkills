package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

//TODO 待修复
class WallClimbingSkill : PassiveSkill(
    Settings(
        id = "wall_climbing",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 15,
        rarity = SkillRarity.RARE
    )
), ClimbingTrigger, AutoStopTrigger, AutoTrigger, SendPlayerDataTrigger {

    init {
        settings.addParameter(
            name = PARAM_CLIMB_DURATION,
            baseValue = DEFAULT_CLIMB_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT
        )
    }

    override fun isClimbing(player: PlayerEntity): Boolean = player.isUsing() && player.shouldClimb()

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CLIMB_DURATION, player, DEFAULT_CLIMB_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
        player.getPersistentData().remove(NBT_HORIZONTAL_COLLISION)
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.isReady() && player.shouldClimb()

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super<AutoStopTrigger>.tick(player, usedTime)
        if (!player.isUsing()) return

        val horizontalCollision = player.shouldClimb()
        val data = player.getData(this)
        val oldSpeed = data?.usingSpeed
        if (!horizontalCollision) {
            data?.usingSpeed = -1
            if (usedTime <= 0) {
                data?.usingSpeed = 1
                player.getPersistentData().remove(NBT_HORIZONTAL_COLLISION)
                player.stopUsing()
            }
        } else if (data?.usingSpeed == -1) {
            data.usingSpeed = 1
        }
        if (oldSpeed != data?.usingSpeed) {
            player.syncData()
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound =
        data.apply { putBoolean(NBT_HORIZONTAL_COLLISION, player.horizontalCollision) }

    override fun getSendTime(): SendTime = SendTime.PREDICATE

    override fun shouldSendData(player: PlayerEntity): Boolean =
        player is ServerPlayerEntity || player.horizontalCollision != player.wasHorizontalCollision

    private fun PlayerEntity.shouldClimb(): Boolean =
        (horizontalCollision || !world.isClient && getPersistentData().getBoolean(NBT_HORIZONTAL_COLLISION))
            && !abilities.flying
            && (!hasEquipped(Skills.WALL_JUMP) || isOnGround || isUsing())

    companion object {

        // Default Values
        private const val DEFAULT_CLIMB_DURATION = 15 * 20

        // Parameter Names
        private const val PARAM_CLIMB_DURATION = "climb_duration"  // 攀爬持续时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间

        // NBT Keys
        private const val NBT_HORIZONTAL_COLLISION = "HorizontalCollision"  // 水平碰撞
    }
}