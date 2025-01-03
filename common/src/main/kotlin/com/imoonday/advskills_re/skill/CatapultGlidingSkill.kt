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
            .addParameter("fly_out_sound", ModSounds.DASH)
            .addParameter(
                name = "charge_time",
                baseValue = 3 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "velocity_multiplier",
                baseValue = 1.0,
                enhancementId = "multiplier",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult =
        if (!user.canUse()) failedResult() else if (user.isFallFlying) fallFlyingResult() else super.use(user)

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("charge_time", player, 3 * 20, 0)

    override fun onPress(player: ServerPlayerEntity): UseResult =
        if (!player.canUse()) failedResult() else if (player.isFallFlying) fallFlyingResult() else super.onPress(player)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        if (!player.canUse()) return failedResult()
        if (player.isFallFlying) return fallFlyingResult()
        player.stopAndCooldown()
        player.playSoundFromParam("fly_out_sound", ModSounds.DASH.get())
        player.setOnGround(false)
        player.startFallFlying()
        val multiplier = getDoubleParam("velocity_multiplier", player, 1.0, 0.0)
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
}