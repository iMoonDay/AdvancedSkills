package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.hit.*
import kotlin.math.*

class GrapplingHookSkill : LongPressSkill(
    Settings(
        id = "grappling_hook",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 15,
        rarity = SkillRarity.EPIC
    )
), UsingRenderTrigger, WorldRendererTrigger, CrosshairTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings.addParameter(
            name = PARAM_HOOK_DURATION,
            baseValue = DEFAULT_HOOK_DURATION,
            enhancementId = ENHANCEMENT_DURATION,
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.INT_PERCENT,
            genericText = true
        ).addParameter(
            name = PARAM_HOOK_RANGE,
            baseValue = DEFAULT_HOOK_RANGE,
            enhancementId = ENHANCEMENT_RANGE,
            value = 4.0,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatter.FLOAT
        )
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

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_HOOK_DURATION, player, DEFAULT_HOOK_DURATION, 0)

    override fun getCrosshair(player: PlayerEntity): Crosshair =
        if (player.isReady() && player.raycastBlock(getMaxDistance(player)).type == HitResult.Type.BLOCK)
            Crosshairs.RING else Crosshairs.NONE

    private fun getMaxDistance(player: PlayerEntity) = 
        getDoubleParam(PARAM_HOOK_RANGE, player, DEFAULT_HOOK_RANGE)

    companion object {
        // Default Values
        private const val DEFAULT_HOOK_DURATION = 3 * 20
        private const val DEFAULT_HOOK_RANGE = 30.0

        // Parameter Names
        private const val PARAM_HOOK_DURATION = "hook_duration"  // 钩爪持续时间
        private const val PARAM_HOOK_RANGE = "hook_range"  // 钩爪范围

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_RANGE = "range"  // 对应钩爪范围
    }
}