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

    init {
        this.settings.addParameter("effect_sound", SoundEvents.ITEM_TOTEM_USE)

        addParameter(
            name = "healing_amount_multiplier",
            baseValue = 0.1f,
            enhancementId = "healing_multiplier",
            value = 0.1f,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 4,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "damage_multiplier",
            baseValue = 1.0f,
            enhancementId = "damage_multiplier",
            value = -0.1f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "damage_interval",
            baseValue = 20,
            enhancementId = "interval",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun allowDeath(player: ServerPlayerEntity, source: DamageSource, amount: Float): Boolean {
        if (player.isUsing() || player.isCooling()) return true
        player.health = player.maxHealth
        player.startUsing()
        player.playSoundFromParam("effect_sound", SoundEvents.ITEM_TOTEM_USE)
        return false
    }

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float {
        if (player.isUsing() && amount > 0) {
            val healingAmount = amount * getFloatParam("healing_amount_multiplier", player, 0.1f, 0.0f)
            player.heal(healingAmount)
        }
        return amount
    }

    override fun serverTick(player: ServerPlayerEntity, usedTime: Int) {
        if (!player.isUsing()) return

        val interval = getIntParam("damage_interval", player, 20, 1)
        if (usedTime % interval == 0) {
            val multiplier = getFloatParam("damage_multiplier", player, 1.0f, 0.0f)
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
