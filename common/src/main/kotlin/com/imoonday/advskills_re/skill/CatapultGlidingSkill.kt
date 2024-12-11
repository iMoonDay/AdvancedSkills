package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.item.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class CatapultGlidingSkill : LongPressSkill(
    id = "catapult_gliding",
    types = listOf(SkillType.MOVEMENT),
    cooldown = 30,
    rarity = SkillRarity.RARE,
    sound = ModSounds.DASH,
    enhancements = setOf(SkillEnhancements.CHARGE_TIME, SkillEnhancements.VELOCITY)
) {

    override fun getMaxPressTime(): Int = 3 * 20

    override fun use(user: ServerPlayerEntity): UseResult =
        if (!user.canUse()) failedResult() else if (user.isFallFlying) fallFlyingResult() else super.use(user)

    override fun onPress(player: ServerPlayerEntity): UseResult =
        if (!player.canUse()) failedResult() else if (player.isFallFlying) fallFlyingResult() else super.onPress(player)

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        if (!player.canUse()) return failedResult()
        if (player.isFallFlying) return fallFlyingResult()
        player.stopAndCooldown()
        player.playSkillSound()
        player.setOnGround(false)
        player.startFallFlying()
        val multiplier = 1 + player.getEnhancementLvl(SkillEnhancements.VELOCITY) * 0.1
        val progress = pressedTime.toDouble() / getModifiedPersistTime(player) * multiplier
        player.velocity =
            player.rotationVector.normalize().multiply(1.5 * progress, 0.0, 1.5 * progress)
                .withAxis(Direction.Axis.Y, 3.0 * progress)
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