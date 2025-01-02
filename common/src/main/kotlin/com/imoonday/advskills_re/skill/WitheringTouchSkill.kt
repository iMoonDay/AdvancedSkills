package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class WitheringTouchSkill : Skill(
    Settings(
        id = "withering_touch",
        types = listOf(SkillType.ENHANCEMENT),
        cooldown = 20,
        rarity = SkillRarity.EPIC
    )
), UsingProgressTrigger, PostAttackTrigger, DangerTrigger, UnequipTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("max_duration", 20 * 9)
            .addParameter("success_sound", SoundEvents.ENTITY_WITHER_SKELETON_HURT)
            .addParameter(
                name = "max_uses",
                baseValue = 3.0,
                enhancementId = "uses",
                value = 1.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "wither_duration",
                baseValue = 3 * 20,
                enhancementId = "duration",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "wither_amplifier",
                baseValue = 0,
                enhancementId = "amplifier",
                value = 1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (!player.isUsing() || source.isIndirect) return

        val baseDuration = getIntParam("wither_duration", player, 3 * 20)
        val maxDuration = getIntParam("max_duration", player, 20 * 9)
        val baseAmplifier = getIntParam("wither_amplifier", player, 0)
        val (duration, amplifier) = target.getStatusEffect(StatusEffects.WITHER)
            ?.let { (it.duration + baseDuration).coerceAtMost(maxDuration) to it.amplifier + baseAmplifier }
            ?: (baseDuration to baseAmplifier)
        target.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, duration, amplifier))
        val successSound = getSoundEventParam("success_sound", SoundEvents.ENTITY_WITHER_SKELETON_HURT)
        if (successSound != null) {
            target.playSoundIfNotSilent(successSound)
        }

        val data = player.getActiveData()
        data.putInt("attackCount", data.getInt("attackCount") + 1)
        if (data.getInt("attackCount") >= getIntParam("max_uses", player, 3)) {
            player.stopAndCooldown()
        } else {
            player.syncData()
        }
    }

    override fun getProgress(player: PlayerEntity): Double =
        1 - player.getActiveData().getInt("attackCount") / getDoubleParam("max_uses", player, 3.0)

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}