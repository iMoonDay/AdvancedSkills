package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.registry.tag.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class ResuscitationSkill : Skill(
    id = "resuscitation",
    types = listOf(SkillType.PASSIVE, SkillType.DEFENSE),
    cooldown = 300,
    rarity = Rarity.LEGENDARY
), DeathTrigger, AutoStopTrigger, DamageTrigger {

    override val persistTime: Int = 2 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || player.isCooling()) return true
        player.health = 1.0f
        player.startUsing()
        player.startCooling()
        player.playSound(SoundEvents.ITEM_TOTEM_USE)
        player.addStatusEffect(StatusEffectInstance(StatusEffects.REGENERATION, 30 * 20))
        return false
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean = player.isUsing()
}
