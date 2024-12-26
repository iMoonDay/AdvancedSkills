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
    id = "primary_confinement",
    types = listOf(SkillType.CONTROL),
    cooldown = 12,
    rarity = SkillRarity.SUPERB,
    sound = SoundEvents::BLOCK_ENCHANTMENT_TABLE_USE,
    enhancements = setOf(
        SkillEnhancements.CHARGE_TIME,
        SkillEnhancements.RANGE,
        SkillEnhancements.CHANCE,
        SkillEnhancements.STATUS_EFFECT_DURATION
    )
), UsingRenderTrigger, CrosshairTrigger, TargetRenderTrigger {

    override val timeParameterName: String = "charge_time"

    init {
        addEnhanceableParameter(timeParameterName, 5 * 20, "time", -0.16f, Enhancement.Type.MULTIPLY, 5) { (it * 100).toInt() }
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        player.swingHand(Hand.MAIN_HAND, true)
        player.raycastLivingEntity(getRange(player))?.takeIf { it.type == HitResult.Type.ENTITY }?.let {
            val extraChance = player.getEnhancementLvl(SkillEnhancements.CHANCE) * 0.04f
            if (player.random.nextFloat() < (0.8f + extraChance) * pressedTime / getPersistTime(player)) {
                val duration = getEnhancedValue(player, SkillEnhancements.STATUS_EFFECT_DURATION, 3 * 20)
                (it.entity as LivingEntity).addStatusEffect(
                    StatusEffectInstance(
                        ModEffects.CONFINEMENT.get(),
                        duration,
                        0,
                        false,
                        false,
                        true
                    )
                )
                return UseResult.success(message("success"))
            }
            return UseResult.pass(message("failed"))
        }
        return UseResult.pass(message("empty"))
    }

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
        5.0 + player.getEnhancementLvl(SkillEnhancements.RANGE)
}