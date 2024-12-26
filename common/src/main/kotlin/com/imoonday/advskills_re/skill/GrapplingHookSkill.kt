package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
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
    rarity = SkillRarity.EPIC,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME, SkillEnhancements.DISTANCE)
), UsingRenderTrigger, WorldRendererTrigger, CrosshairTrigger {

    init {
        addEnhanceableParameter(timeParameterName, 3 * 20, "time", 0.2f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun onPress(player: ServerPlayerEntity): UseResult {
        val raycast = player.raycastBlock(getMaxDistance(player))
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
                    player.stopAndCooldown()
                    return@run
                }
                val rotation = player.rotationVector
                val newVelocity = add(
                    rotation.x,
                    player.height.toDouble() / 2.0 + rotation.y,
                    rotation.z
                ).subtract(pos).normalize()
                    .multiply((distance / getMaxDistance(player)) + 1)
                player.addVelocity((newVelocity - player.velocity).multiply(0.5))
                player.velocityDirty = true
            }
        }
        super.tick(player, usedTime)
    }

    override fun getCrosshair(player: PlayerEntity): Crosshair =
        if (player.isReady() && player.raycastBlock(getMaxDistance(player)).type == HitResult.Type.BLOCK)
            Crosshairs.RING else Crosshairs.NONE

    fun getMaxDistance(player: PlayerEntity) = 30.0 + player.getEnhancementLvl(SkillEnhancements.DISTANCE) * 4.0
}