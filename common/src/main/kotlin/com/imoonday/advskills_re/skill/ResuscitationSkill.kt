package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.server.network.*

class ResuscitationSkill : Skill(
    id = "resuscitation",
    types = listOf(SkillType.PASSIVE, SkillType.DEFENSE),
    cooldown = 300,
    rarity = SkillRarity.LEGENDARY,
    enhancements = setOf(SkillEnhancements.PERSISTENT_TIME)
), DeathTrigger, AutoStopTrigger, DamageTrigger {

    override val persistTime: Int = 2 * 20

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isCooling()) return true
        player.health = 1.0f
        player.startUsing()
        player.startCooling()
        player.world.sendEntityStatus(player, EntityStatuses.USE_TOTEM_OF_UNDYING)
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
