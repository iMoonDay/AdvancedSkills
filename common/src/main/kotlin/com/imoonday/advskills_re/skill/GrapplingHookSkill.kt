package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*
import kotlin.math.*

class GrapplingHookSkill : LongPressSkill(
    id = "grappling_hook",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 15,
    rarity = SkillRarity.EPIC
), UsingRenderTrigger, WorldRendererTrigger, CrosshairTrigger {

    override fun getMaxPressTime(): Int = 3 * 20

    override fun onPress(player: ServerPlayerEntity): UseResult {
        val raycast = player.raycastBlock(MAX_DISTANCE)
        return if (raycast.type == HitResult.Type.BLOCK) {
            UseResult.startUsing(player, this, NbtUtils.writeVec3dToTag(raycast.pos))
        } else {
            UseResult.fail(failedMessage())
        }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
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
                        player.stopAndCooldown()
                    }
                    return@run
                }
                val rotation = player.rotationVector
                val newVelocity = add(
                    rotation.x,
                    player.height.toDouble() / 2.0 + rotation.y,
                    rotation.z
                ).subtract(pos).normalize()
                    .multiply((distance / MAX_DISTANCE) + 1)
                player.velocityDirty = true
                player.addVelocity((newVelocity - player.velocity).multiply(0.5))
            }
        }
        super.tick(player, usedTime)
    }

    override fun getCrosshair(player: PlayerEntity): Crosshair =
        if (player.isReady() && player.raycastBlock(MAX_DISTANCE).type == HitResult.Type.BLOCK)
            Crosshairs.RING else Crosshairs.NONE

    companion object {

        private const val MAX_DISTANCE = 30.0
    }
}