package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.UseResult
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.effect.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class DyingCounterattackSkill : Skill(
    Settings(
        id = "dying_counterattack",
        types = listOf(SkillType.PASSIVE),
        cooldown = 180,
        rarity = SkillRarity.EPIC
    )
), DeathTrigger, PersistentTrigger, AttackTrigger, TickTrigger, UnequipTrigger, StatusEffectTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(PARAM_REVIVE_SOUND, DEFAULT_REVIVE_SOUND)
            .addParameter(
                name = PARAM_HEAL_RATIO,
                baseValue = DEFAULT_HEAL_RATIO,
                enhancementId = ENHANCEMENT_HEAL,
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 4,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DAMAGE_PENALTY,
                baseValue = DEFAULT_DAMAGE_PENALTY,
                enhancementId = ENHANCEMENT_PENALTY,
                value = -0.1f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_WITHER_INTERVAL,
                baseValue = DEFAULT_WITHER_INTERVAL,
                enhancementId = ENHANCEMENT_INTERVAL,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isUsing() || player.isCooling()) return true
        player.health = player.maxHealth
        player.startUsing()
        player.playSoundFromParam(PARAM_REVIVE_SOUND, DEFAULT_REVIVE_SOUND)
        return false
    }

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (player.isUsing() && amount > 0) {
            val healingAmount = amount * getFloatParam(PARAM_HEAL_RATIO, player, DEFAULT_HEAL_RATIO, 0.0f)
            player.heal(healingAmount)
        }
        return amount
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return

        val interval = getIntParam(PARAM_WITHER_INTERVAL, player, DEFAULT_WITHER_INTERVAL, 1)
        if (usedTime % interval == 0) {
            val multiplier = getFloatParam(PARAM_DAMAGE_PENALTY, player, DEFAULT_DAMAGE_PENALTY, 0.0f)
            val amount = 2.0f * (usedTime / 200 + if (usedTime % 200 == 0) 0 else 1) * multiplier
            player.damage(player.damageSources.wither(), amount)
        }
        if (player.isDead) player.startCooling()
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean =
        !player.isUsing()

    override fun shouldHaveStatusEffect(player: PlayerEntity, effect: StatusEffect): Boolean =
        player.isUsing() && effect == StatusEffects.WITHER || super.shouldHaveStatusEffect(player, effect)

    companion object {

        // Default Values
        private const val DEFAULT_HEAL_RATIO = 0.1f
        private const val DEFAULT_DAMAGE_PENALTY = 1.0f
        private const val DEFAULT_WITHER_INTERVAL = 20
        private val DEFAULT_REVIVE_SOUND = SoundEvents.ITEM_TOTEM_USE

        // Parameter Names
        private const val PARAM_REVIVE_SOUND = "revive_sound"  // 复活音效
        private const val PARAM_HEAL_RATIO = "heal_ratio"  // 治疗比例
        private const val PARAM_DAMAGE_PENALTY = "damage_penalty"  // 伤害惩罚
        private const val PARAM_WITHER_INTERVAL = "wither_interval"  // 凋零间隔

        // Enhancement IDs
        private const val ENHANCEMENT_HEAL = "heal"  // 对应治疗比例
        private const val ENHANCEMENT_PENALTY = "penalty"  // 对应伤害惩罚
        private const val ENHANCEMENT_INTERVAL = "interval"  // 对应凋零间隔
    }
}
