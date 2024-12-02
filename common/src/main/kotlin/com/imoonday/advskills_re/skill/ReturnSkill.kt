package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

//TODO 服务器跨维度传送坐标异常
class ReturnSkill : LongPressSkill(
    id = "return",
    types = listOf(SkillType.FUNCTION, SkillType.MOVEMENT),
    cooldown = 0,
    rarity = Rarity.SUPERB,
    sound = ModSounds.RETURN
) {

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing {
            NbtUtils.writeEntityPositionToTag(player.pos, it)
        }
        return getChargingResult()
    }

    override fun getMaxPressTime(): Int = 5 * 20

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing()) {
            var usePos: Vec3d? = null
            player.getUsingData()?.let { data ->
                NbtUtils.readEntityPositionFromTag(data)?.let { pos ->
                    usePos = pos
                }
            }
            if (usePos != player.pos) {
                player.stopUsing()
                player.sendMessage(failedMessage(), true)
            }
        }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        if (pressedTime < getMaxPressTime()) {
            return UseResult.fail(failedMessage())
        }
        val spawnAngle = player.spawnAngle
        val pos = player.spawnPointPosition ?: player.serverWorld.spawnPos
        val world = player.server.getWorld(player.spawnPointDimension) ?: player.server.overworld
        val teleportPos =
            PlayerEntity.findRespawnPosition(world, pos, spawnAngle, false, true).orElse(pos.toCenterPos())
        player.playSkillSound()
        player.teleport(
            world, teleportPos.x, teleportPos.y, teleportPos.z,
            emptySet(),
            spawnAngle, 0f
        )
        while (!world.isSpaceEmpty(player) && player.y < world.topY) {
            player.setPosition(player.x, player.y + 1.0, player.z)
        }
        return UseResult.success()
    }
}