package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
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
    cooldown = 0,
    rarity = Rarity.EPIC
), UsingProgressTrigger, PostAttackTrigger, DangerTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun postAttack(source: DamageSource, player: ServerPlayerEntity, target: LivingEntity) {
        super.postAttack(source, player, target)
        if (player.isUsing() && !source.isIndirect) {
            val (duration, amplifier) = target.getStatusEffect(StatusEffects.WITHER)
                ?.let { (it.duration + 20 * 3).coerceAtMost(20 * 9) to it.amplifier + 1 } ?: (20 * 3 to 0)
            target.addStatusEffect(StatusEffectInstance(StatusEffects.WITHER, duration, amplifier))
            target.playSoundIfNotSilent(SoundEvents.ENTITY_WITHER_SKELETON_HURT)
            val data = player.getActiveData()
            data.putInt("attackCount", data.getInt("attackCount") + 1)
            if (data.getInt("attackCount") >= 3) {
                player.stopAndCooldown()
            } else {
                player.syncData()
            }
        }
    }

    override fun getProgress(player: PlayerEntity): Double = 1 - player.getActiveData().getInt("attackCount") / 3.0
}