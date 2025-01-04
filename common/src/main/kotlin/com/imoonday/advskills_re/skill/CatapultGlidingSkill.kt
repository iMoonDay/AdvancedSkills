package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class CatapultGlidingSkill : LongPressSkill(
    Settings(
        id = "catapult_gliding",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 30,
        rarity = SkillRarity.RARE
    )
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND)
            .addParameter(
                name = PARAM_CHARGE_TIME,
                baseValue = DEFAULT_CHARGE_TIME,
                enhancementId = ENHANCEMENT_CHARGE_TIME,
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_CATAPULT_FORCE,
                baseValue = DEFAULT_CATAPULT_FORCE,
                enhancementId = ENHANCEMENT_FORCE,
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult =
        if (!user.canUse()) failedResult() else if (user.isFallFlying) fallFlyingResult() else super.use(user)

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_CHARGE_TIME, player, DEFAULT_CHARGE_TIME, 0)

    override fun onPress(player: ServerPlayerEntity): UseResult =
        if (!player.canUse()) failedResult() else if (player.isFallFlying) fallFlyingResult() else super.onPress(player)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        if (!player.canUse()) return failedResult()
        if (player.isFallFlying) return fallFlyingResult()
        player.stopAndCooldown()
        player.playSoundFromParam(PARAM_LAUNCH_SOUND, DEFAULT_LAUNCH_SOUND.get())
        player.setOnGround(false)
        player.startFallFlying()
        val multiplier = getDoubleParam(PARAM_CATAPULT_FORCE, player, DEFAULT_CATAPULT_FORCE, 0.0)
        val progress = pressedTime.toDouble() / getMaxUseTime(player) * multiplier
        player.velocity =
            player.rotationVector.normalize().multiply(1.5, 0.0, 1.5)
                .withAxis(Direction.Axis.Y, 3.0) * progress
        player.updateVelocity()
        return UseResult.success()
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (player.isUsing() && !player.canUse()) player.stopUsing()
        super.serverTick(player, usedTime)
    }

    private fun failedResult() = UseResult.fail(failedMessage())

    private fun fallFlyingResult() = UseResult.fail(message("fallFlying"))

    private fun ServerPlayerEntity.canUse(): Boolean {
        val stack = getEquippedStack(EquipmentSlot.CHEST)
        return (stack.item is ElytraItem && ElytraItem.isUsable(stack))
    }

    companion object {
        // Default Values
        private const val DEFAULT_CHARGE_TIME = 3 * 20
        private const val DEFAULT_CATAPULT_FORCE = 1.0
        private val DEFAULT_LAUNCH_SOUND = ModSounds.DASH

        // Parameter Names
        private const val PARAM_LAUNCH_SOUND = "launch_sound"  // 发射音效
        private const val PARAM_CHARGE_TIME = "charge_time"  // 蓄力时间
        private const val PARAM_CATAPULT_FORCE = "catapult_force"  // 弹射力度

        // Enhancement IDs
        private const val ENHANCEMENT_CHARGE_TIME = "charge_time"  // 对应蓄力时间
        private const val ENHANCEMENT_FORCE = "force"  // 对应弹射力度
    }
}