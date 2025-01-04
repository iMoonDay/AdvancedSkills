package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class TauntSkill : Skill(
    Settings(
        id = "taunt",
        types = listOf(SkillType.UTILITY, SkillType.DEFENSE),
        cooldown = 30,
        rarity = SkillRarity.UNCOMMON
    )
), DamageTrigger, AutoStopTrigger, UsingRenderTrigger, TauntTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = PARAM_TAUNT_DURATION,
                baseValue = DEFAULT_TAUNT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DAMAGE_REDUCTION,
                baseValue = DEFAULT_DAMAGE_REDUCTION,
                enhancementId = ENHANCEMENT_REDUCTION,
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.startUsing(user, this)

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float =
        if (!player.isUsing() || attacker !is Servant) amount
        else amount * (1f - getFloatParam(PARAM_DAMAGE_REDUCTION, player, DEFAULT_DAMAGE_REDUCTION, max = 1.0f))

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean = !player.isUsing()

    override fun getMaxUseTime(player: PlayerEntity): Int = 
        getIntParam(PARAM_TAUNT_DURATION, player, DEFAULT_TAUNT_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        super.onStop(player)
        player.startCooling()
    }

    companion object {
        // Default Values
        private const val DEFAULT_TAUNT_DURATION = 15 * 20
        private const val DEFAULT_DAMAGE_REDUCTION = 0.25f

        // Parameter Names
        private const val PARAM_TAUNT_DURATION = "taunt_duration"  // 嘲讽持续时间
        private const val PARAM_DAMAGE_REDUCTION = "damage_reduction"  // 伤害减免

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_REDUCTION = "reduction"  // 对应减免
    }
}