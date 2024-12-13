package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
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
    id = "withering_touch",
    types = listOf(SkillType.ENHANCEMENT),
    cooldown = 20,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(
        SkillEnhancements.USE_COUNT,
        SkillEnhancements.STATUS_EFFECT_DURATION,
        SkillEnhancements.STATUS_EFFECT_AMPLIFIER
    )
), UsingProgressTrigger, PostAttackTrigger, DangerTrigger, UnequipTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (!player.isUsing() || source.isIndirect) return

        var (duration, amplifier) = target.getStatusEffect(StatusEffects.WITHER)
            ?.let { (it.duration + 20 * 3).coerceAtMost(20 * 9) to it.amplifier + 1 } ?: (20 * 3 to 0)
        duration = getEnhancedValue(player, SkillEnhancements.STATUS_EFFECT_DURATION, duration)
        amplifier += player.getEnhancementLvl(SkillEnhancements.STATUS_EFFECT_AMPLIFIER)
        target.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, duration, amplifier))
        target.playSoundIfNotSilent(SoundEvents.ENTITY_WITHER_SKELETON_HURT)
        val data = player.getActiveData()
        data.putInt("attackCount", data.getInt("attackCount") + 1)
        if (data.getInt("attackCount") >= getMaxUseCount(player)) {
            player.stopAndCooldown()
        } else {
            player.syncData()
        }
    }

    override fun getProgress(player: PlayerEntity): Double =
        1 - player.getActiveData().getInt("attackCount") / getMaxUseCount(player)

    private fun getMaxUseCount(player: PlayerEntity) = 3.0 + player.getEnhancementLvl(SkillEnhancements.USE_COUNT)

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super.postUnequipped(player, slot)
        if (player.isUsing()) {
            player.startCooling()
        }
    }
}