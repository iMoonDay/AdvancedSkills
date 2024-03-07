package com.imoonday.skill

import com.imoonday.trigger.AutoStopTrigger
import com.imoonday.trigger.DamageTrigger
import com.imoonday.trigger.DeathTrigger
import com.imoonday.util.SkillType
import com.imoonday.util.UseResult
import com.imoonday.util.playSound
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents

class ResuscitationSkill : Skill(
    id = "resuscitation",
    types = arrayOf(SkillType.PASSIVE, SkillType.DEFENSE),
    cooldown = 300,
    rarity = Rarity.LEGENDARY
), DeathTrigger, AutoStopTrigger, DamageTrigger {

    override fun getPersistTime(): Int = 20 * 2

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true
        player.health = 1.0f
        player.startUsing()
        player.startCooling()
        player.playSound(SoundEvents.ITEM_TOTEM_USE)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 30))
        return false
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount else 0.0f
}
