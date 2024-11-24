package com.imoonday.skill

import com.imoonday.component.*
import com.imoonday.trigger.*
import com.imoonday.util.SkillSlot
import com.imoonday.util.SkillType
import com.imoonday.util.getData
import com.imoonday.util.syncData
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*

class WallClimbingSkill : PassiveSkill(
    id = "wall_climbing",
    types = listOf(SkillType.PASSIVE, SkillType.MOVEMENT),
    cooldown = 15,
    rarity = Rarity.RARE,
), ClimbingTrigger, AutoStopTrigger, AutoTrigger, SendPlayerDataTrigger {

    override fun isClimbing(player: PlayerEntity): Boolean =
        player.isUsing() && player.shouldClimb()

    override val persistTime: Int = 20 * 15
    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
    }

    override fun shouldStart(player: ServerPlayerEntity): Boolean = player.isReady() && player.shouldClimb()

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing()) {
            val horizontalCollision = player.properties.getBoolean(HORIZONTAL_COLLISION_KEY)
            val data = player.getData(this)
            if (!horizontalCollision) {
                data?.usingSpeed = -1
                if (usedTime <= 0) {
                    data?.usingSpeed = 1
                    player.stopUsing()
                }
            } else if (data?.usingSpeed == -1) {
                data.usingSpeed = 1
            }
            player.syncData()
        }
    }

    override fun write(player: PlayerEntity, data: NbtCompound): NbtCompound =
        data.apply { putBoolean(HORIZONTAL_COLLISION_KEY, player.horizontalCollision) }

    override fun apply(player: ServerPlayerEntity, data: NbtCompound) {
        player.properties.putBoolean(HORIZONTAL_COLLISION_KEY, data.getBoolean(HORIZONTAL_COLLISION_KEY))
    }

    override fun getSendTime(): SendTime = SendTime.ALWAYS
    private fun PlayerEntity.shouldClimb(): Boolean =
        (horizontalCollision || properties.getBoolean(HORIZONTAL_COLLISION_KEY)) && !abilities.flying

    companion object {

        private const val HORIZONTAL_COLLISION_KEY = "horizontalCollision"
    }
}