package com.imoonday.skills

import com.imoonday.components.isUsingSkill
import com.imoonday.components.startCooling
import com.imoonday.components.startUsingSkill
import com.imoonday.triggers.AutoStopTrigger
import com.imoonday.triggers.DeathTrigger
import com.imoonday.triggers.PlayerDamageTrigger
import com.imoonday.utils.*
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
), DeathTrigger, AutoStopTrigger, PlayerDamageTrigger {

    override val persistTime: Int = 20 * 2
    override val skill: Skill
        get() = this

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name.string)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) return true
        player.health = 1.0f
        player.startUsingSkill(skill)
        player.startCooling(skill)
        player.world.playSound(null, player.blockPos, SoundEvents.ITEM_TOTEM_USE, SoundCategory.PLAYERS)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 20 * 30))
        return false
    }

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsingSkill(this)) amount else 0.0f
}
