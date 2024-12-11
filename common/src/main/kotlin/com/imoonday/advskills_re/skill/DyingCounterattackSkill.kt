package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.playSound
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class DyingCounterattackSkill : Skill(
    id = "dying_counterattack",
    types = listOf(SkillType.PASSIVE),
    cooldown = 180,
    rarity = SkillRarity.EPIC,
    enhancements = setOf(SkillEnhancements.HEALING_AMOUNT, SkillEnhancements.USE_COST)
), DeathTrigger, PersistentTrigger, AttackTrigger, TickTrigger, UnequipTrigger, StatusEffectTrigger {

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isUsing() || player.isCooling()) return true
        player.health = player.maxHealth
        player.startUsing()
        player.playSound(SoundEvents.ITEM_TOTEM_USE)
        return false
    }

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (player.isUsing() && amount > 0) {
            val healingAmount = getEnhancedValue(player, SkillEnhancements.HEALING_AMOUNT, amount / 10)
            player.heal(healingAmount)
        }
        return amount
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return
        if (usedTime % 20 == 0) {
            val multiplier = 1 - player.getEnhancementLvl(SkillEnhancements.USE_COST) * 0.1f
            val amount = 2.0f * (usedTime / 200 + if (usedTime % 200 == 0) 0 else 1) * multiplier
            player.damage(player.damageSources.wither(), amount)
        }
        if (player.isDead) player.startCooling()
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean =
        !player.isUsing()

    override fun shouldHaveStatusEffect(player: PlayerEntity, effect: StatusEffect): Boolean =
        player.isUsing() && effect == StatusEffects.WITHER || super.shouldHaveStatusEffect(player, effect)
}
