package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*
import net.minecraft.server.world.*
import net.minecraft.util.math.*

//TODO 服务器跨维度传送坐标异常?
class ReturnSkill : LongPressSkill(
    Settings(
        id = "return",
        types = listOf(SkillType.UTILITY, SkillType.MOVEMENT),
        cooldown = 0,
        rarity = SkillRarity.SUPERB
    )
), UsingRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_RETURN_SOUND, DEFAULT_RETURN_SOUND)
            .addParameter(
                name = PARAM_CHARGE_DURATION,
                baseValue = DEFAULT_CHARGE_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            )
    }

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.startUsing {
            NbtUtils.writeEntityPositionToTag(player.pos, it)
        }
        return getChargingResult()
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        super.serverTick(player, usedTime)
        if (player.isUsing()) {
            val startPos = NbtUtils.readEntityPositionFromTag(player.getActiveData())
            if (startPos != player.pos) {
                player.stopUsing()
                player.sendMessage(failedMessage(), true)
            } else {
                var (_, world, targetPos) = getTeleportInfo(player)
                val dimensions = player.getDimensions(player.pose)
                while (!world.isSpaceEmpty(player, dimensions.getBoxAt(targetPos)) && targetPos.y < world.topY) {
                    targetPos = targetPos.offset(Direction.UP, 1.0)
                }

                val random = player.random
                val height = player.height
                player.spawnParticles(
                    ParticleTypes.PORTAL,
                    false,
                    targetPos.add(
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

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_CHARGE_DURATION, player, DEFAULT_CHARGE_DURATION, 0)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        if (pressedTime < getMaxUseTime(player)) {
            return UseResult.fail(failedMessage())
        }
        val (spawnAngle, world, targetPos) = getTeleportInfo(player)
        player.playSoundFromParam(PARAM_RETURN_SOUND, DEFAULT_RETURN_SOUND.get())
        player.teleport(
            world, targetPos.x, targetPos.y, targetPos.z,
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

    companion object {

        // Default Values
        private const val DEFAULT_CHARGE_DURATION = 5 * 20
        private val DEFAULT_RETURN_SOUND = ModSounds.RETURN

        // Parameter Names
        private const val PARAM_RETURN_SOUND = "return_sound"  // 返回音效
        private const val PARAM_CHARGE_DURATION = "charge_duration"  // 蓄力时间

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "charge_time"  // 对应持续时间
    }
}