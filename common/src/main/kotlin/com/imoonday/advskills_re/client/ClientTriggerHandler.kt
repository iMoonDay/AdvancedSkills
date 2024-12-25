package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

object ClientTriggerHandler {

    @JvmStatic
    fun isGlowing(entity: Entity): Boolean {
        val player = clientPlayer ?: return false
        return player.anyTrigger<GlowingTrigger> { it.isGlowing(entity, player) }
    }

    /**
     * Should invert mouse
     *
     * @return Pair of should invert horizontal mouse and should invert vertical mouse
     */
    @JvmStatic
    fun shouldInvertMouse(): Pair<Boolean, Boolean> {
        val player = clientPlayer ?: return false to false
        return player.getTriggers<InvertMouseTrigger>().run {
            map { it.shouldInvertMouseX(player) }.any { it } to map { it.shouldInvertMouseY(player) }.any { it }
        }
    }

    /**
     * Should invert input
     *
     * @return Pair of should invert horizontal input and should invert vertical input
     */
    @JvmStatic
    fun shouldInvertInput(): Pair<Boolean, Boolean> {
        val player = clientPlayer ?: return false to false
        return player.getTriggers<InvertInputTrigger>().run {
            map { it.shouldInvertHorizontalInput(player) }.any { it } to map { it.shouldInvertVerticalInput(player) }.any { it }
        }
    }

    @JvmStatic
    fun getCameraMovement(original: Float): Float {
        var movement = original
        val player = clientPlayer
        player?.forEachTrigger<CameraUpdateMovementTrigger> { movement = it.getDelta(movement, player) }
        return movement
    }

    @JvmStatic
    fun sendPlayerData(player: PlayerEntity) =
        player.forEachTrigger<SendPlayerDataTrigger>(
            { it.getSendTime().shouldSendOnTick(player, it.getAsSkill()) }
        ) {
            Channels.SEND_PLAYER_DATA_C2S.sendToServer(
                SendPlayerDataC2SPacket(
                    it.getAsSkill(),
                    it.write(player, NbtCompound())
                )
            )
        }

    @JvmStatic
    fun shouldSyncJumpState(player: PlayerEntity): Boolean =
        player.anyTrigger<JumpStateTrigger> { it.shouldSyncJumpState(player) }
}