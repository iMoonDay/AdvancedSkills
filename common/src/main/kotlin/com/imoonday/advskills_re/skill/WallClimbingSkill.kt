package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

class WallClimbingSkill : PassiveSkill(
    id = "wall_climbing",
    extraTypes = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = SkillRarity.RARE,
), ClimbingTrigger, AutoStopTrigger, AutoTrigger, SendPlayerDataTrigger {

    override fun isClimbing(player: PlayerEntity): Boolean = player.isUsing() && player.shouldClimb()

    override val persistTime: Int = 15 * 20

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.isReady() && player.shouldClimb()

    override fun tick(player: PlayerEntity, usedTime: Int) {
        super<AutoStopTrigger>.tick(player, usedTime)
        if (player.isUsing()) {
            val horizontalCollision =
                player.horizontalCollision || player.getPersistentData().getBoolean(HORIZONTAL_COLLISION_KEY)
            val data = player.getData(this)
            val oldSpeed = data?.usingSpeed
            if (!horizontalCollision) {
                data?.usingSpeed = -1
                if (usedTime <= 0) {
                    data?.usingSpeed = 1
                    player.stopUsing()
                }
            } else if (data?.usingSpeed == -1) {
                data.usingSpeed = 1
            }
            if (oldSpeed != data?.usingSpeed) {
                player.syncData()
            }
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound =
        data.apply { putBoolean(HORIZONTAL_COLLISION_KEY, player.horizontalCollision) }

    override fun getSendTime(): SendTime = SendTime.PREDICATE

    override fun shouldSendData(player: PlayerEntity): Boolean =
        player is ServerPlayerEntity || player.horizontalCollision != player.wasHorizontalCollision

    private fun PlayerEntity.shouldClimb(): Boolean =
        (horizontalCollision || getPersistentData().getBoolean(HORIZONTAL_COLLISION_KEY)) && !abilities.flying

    companion object {

        private const val HORIZONTAL_COLLISION_KEY = "horizontalCollision"
    }
}