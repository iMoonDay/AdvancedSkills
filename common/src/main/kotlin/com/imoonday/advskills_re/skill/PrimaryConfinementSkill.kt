package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*
import net.minecraft.util.*
import net.minecraft.util.hit.*

class PrimaryConfinementSkill : LongPressSkill(
    Settings(
        id = "primary_confinement",
        types = listOf(SkillType.CONTROL),
        cooldown = 12,
        rarity = SkillRarity.SUPERB
    )
), UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("confinement_sound", SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE)
            .addParameter(
                name = "charge_time",
                baseValue = 5 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "range",
                baseValue = 5.0,
                enhancementId = "range",
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "success_chance",
                baseValue = 0.8f,
                enhancementId = "chance",
                value = 0.04f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "confinement_duration",
                baseValue = 3 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(getRange(player))?.takeIf { it.type == HitResult.Type.ENTITY }?.let {
            val baseChance = getFloatParam("success_chance", player, 0.8f, max = 1.0f)
            if (player.random.nextFloat() < baseChance * pressedTime / getMaxUseTime(player)) {
                val duration = getIntParam("confinement_duration", player, 3 * 20)
                (it.entity as LivingEntity).addStatusEffect(
                    StatusEffectInstance(ModEffects.CONFINEMENT.get(), duration, 0, false, false, true)
                )
                return UseResult.success(
                    message("success"),
                    getSoundEventParam("confinement_sound", SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE)
                )
            }
            return UseResult.pass(failedMessage())
        }
        return UseResult.pass(message("empty"))
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam("charge_time", player, 5 * 20, 0)

    override fun getCrosshair(player: PlayerEntity): Crosshair {
        player.run {
            if (!isUsing()) return Crosshairs.NONE
            if (raycastLivingEntity(getRange(player))?.type == HitResult.Type.ENTITY) return Crosshairs.CROSS
        }
        return Crosshairs.NONE
    }

    override fun isTarget(clientPlayer: PlayerEntity, entity: LivingEntity): Boolean {
        if (!clientPlayer.isUsing()) return false
        return clientPlayer.raycastLivingEntity(getRange(clientPlayer))?.entity == entity
    }

    private fun getRange(player: PlayerEntity): Double =
        getDoubleParam("range", player, 5.0)
}