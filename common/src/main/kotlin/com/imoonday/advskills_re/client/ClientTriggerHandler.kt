package com.imoonday.advskills_re.client

import com.imoonday.advskills_re.network.*
import com.imoonday.advskills_re.network.c2s.*
import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.gui.hud.InGameHud.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*

object ClientTriggerHandler {

    @JvmStatic
    fun isGlowing(entity: Entity): Boolean {
        val player = clientPlayer
        return player?.getTriggers<GlowingTrigger>()
            ?.map { it.isGlowing(entity, player) }
            ?.any { it } ?: false
    }

    @JvmStatic
    fun shouldInvertMouse(): Pair<Boolean, Boolean> {
        val player = clientPlayer
        return player?.getTriggers<InvertMouseTrigger>()?.run {
            map { it.shouldInvertMouseX(player) }.any { it } to map { it.shouldInvertMouseY(player) }.any { it }
        } ?: (false to false)
    }

    @JvmStatic
    fun shouldInvertInput(): Pair<Boolean, Boolean> {
        val player = clientPlayer
        return player?.getTriggers<InvertInputTrigger>()?.run {
            map { it.shouldInvertHorizontalInput(player) }.any { it } to map { it.shouldInvertVerticalInput(player) }.any { it }
        } ?: (false to false)
    }

    @JvmStatic
    fun getCameraMovement(original: Float): Float {
        var movement = original
        val player = clientPlayer
        player?.getTriggers<CameraUpdateMovementTrigger>()
            ?.forEach { movement = it.getDelta(movement, player) }
        return movement
    }

    @JvmStatic
    fun getHeartType(player: PlayerEntity): HeartType? =
        player.getTriggers<HeartTypeTrigger>()
            .mapNotNull { it.getHeartType(player) }
            .maxByOrNull { it.second }?.first

    @JvmStatic
    fun sendPlayerData(player: PlayerEntity) =
        player.getTriggers<SendPlayerDataTrigger>()
            .filter { it.getSendTime().shouldSendOnTick(player, it.getAsSkill()) }
            .forEach {
                Channels.SEND_PLAYER_DATA_C2S.sendToServer(
                    SendPlayerDataC2SPacket(
                        it.getAsSkill(),
                        it.write(player, NbtCompound())
                    )
                )
            }
}