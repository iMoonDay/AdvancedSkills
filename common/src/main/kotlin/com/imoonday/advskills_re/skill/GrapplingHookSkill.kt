package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.nbt.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*
import kotlin.math.*

class GrapplingHookSkill : LongPressSkill(
    id = "grappling_hook",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = Rarity.EPIC
), UsingRenderTrigger, WorldRendererTrigger, CrosshairTrigger {

    override fun getMaxPressTime(): Int = 3 * 20

    private val maxDistance = 30.0

    override fun onPress(player: ServerPlayerEntity): UseResult {
        val raycast = player.raycastVisualBlock(maxDistance)
        return if (raycast.type == HitResult.Type.BLOCK) {
            UseResult.startUsing(
                player,
                this,
                NbtUtils.writeVec3dToTag(raycast.pos, NbtCompound())
            ).withCooling(false)
        } else {
            UseResult.fail(failedMessage())
        }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopUsing()
        return UseResult.success()
    }

    override fun tick(player: PlayerEntity, usedTime: Int) {
        if (player.isUsing()) {
            player.fallDistance = 0f
            player.stopFallFlying()
            NbtUtils.readVec3d(player.getActiveData())?.run {
                val pos = player.pos
                val distance = distanceTo(pos)
                if (player.blockPos.down() == toBlockPos()
                    || player.calculateAngle(this) > PI / 4.5
                ) {
                    if (!player.world.isClient) {
                        player.stopUsing()
                        player.startCooling()
                    }
                    return@run
                }
                val rotation = player.rotationVector
                val newVelocity = add(
                    rotation.x,
                    player.height.toDouble() / 2.0 + rotation.y,
                    rotation.z
                ).subtract(pos).normalize()
                    .multiply((distance / maxDistance) + 1)
                player.velocityDirty = true
                player.addVelocity((newVelocity - player.velocity).multiply(0.5))
            }
        }
        super.tick(player, usedTime)
    }

    override fun getCrosshair(player: PlayerEntity): Crosshair =
        if (player.isReady() && player.raycastVisualBlock(maxDistance).type == HitResult.Type.BLOCK)
            Crosshairs.RING else Crosshairs.NONE
}