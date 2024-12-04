package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.trigger.renderer.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*
import net.minecraft.util.math.*

//TODO 服务器跨维度传送坐标异常
class ReturnSkill : LongPressSkill(
    id = "return",
    types = listOf(SkillType.UTILITY, SkillType.MOVEMENT),
    cooldown = 0,
    rarity = Rarity.SUPERB,
    sound = ModSounds.RETURN
), UsingRenderTrigger {

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
            val usePos = NbtUtils.readEntityPositionFromTag(player.getActiveData())
            if (usePos != player.pos) {
                player.stopUsing()
                player.sendMessage(failedMessage(), true)
            } else {
                var (_, world, teleportPos) = getTeleportInfo(player)
                val dimensions = player.getDimensions(player.pose)
                while (!world.isSpaceEmpty(player, dimensions.getBoxAt(teleportPos)) && teleportPos.y < world.topY) {
                    teleportPos = teleportPos.offset(Direction.UP, 1.0)
                }

                val random = player.random
                val height = player.height
                player.spawnParticles(
                    ParticleTypes.PORTAL,
                    false,
                    teleportPos.add(
                        random.nextDouble() - 0.5,
                        random.nextDouble() * height,
                        random.nextDouble() - 0.5
                    ),
                    3,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    0.1
                )
            }
        }
    }

    override fun clientTick(player: PlayerEntity, usedTime: Int) {
        super.clientTick(player, usedTime)
        if (player.isUsing()) {
            val world = player.world
            val pos = player.pos
            val random = player.random
            val height = player.height
            for (i in 0..3) {
                world.addParticle(
                    ParticleTypes.PORTAL,
                    pos.x + random.nextDouble() - 0.5,
                    pos.y + random.nextDouble() * height,
                    pos.z + random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5,
                    random.nextDouble() - 0.5
                )
            }
        }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        if (pressedTime < getMaxPressTime()) {
            return UseResult.fail(failedMessage())
        }
        val (spawnAngle, world, teleportPos) = getTeleportInfo(player)
        player.playSkillSound()
        player.teleport(
            world, teleportPos.x, teleportPos.y, teleportPos.z,
            emptySet(),
            spawnAngle, 0f
        )
        while (!world.isSpaceEmpty(player) && player.y < world.topY) {
            player.teleport(player.x, player.y + 1.0, player.z)
        }
        return UseResult.success()
    }

    private fun getTeleportInfo(player: ServerPlayerEntity): Triple<Float, ServerWorld, Vec3d> {
        val spawnAngle = player.spawnAngle
        val pos = player.spawnPointPosition ?: player.serverWorld.spawnPos
        val world = player.server.getWorld(player.spawnPointDimension) ?: player.server.overworld
        val teleportPos =
            PlayerEntity.findRespawnPosition(world, pos, spawnAngle, false, true).orElse(pos.toCenterPos())
        return Triple(spawnAngle, world, teleportPos)
    }
}