package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.UseResult
import com.imoonday.advskills_re.util.isUsing
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class ActiveDefenseSkill : LongPressSkill(
    Settings(
        id = "active_defense",
        types = listOf(SkillType.DEFENSE),
        cooldown = 10,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AttributeTrigger, UsingRenderTrigger {

    override fun initSettings(settings: Settings) {
        super.initSettings(settings)
        settings
            .addParameter(
                name = PARAM_DURATION,
                baseValue = DEFAULT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DEFENSE_VALUE,
                baseValue = DEFAULT_DEFENSE_VALUE,
                enhancementId = ENHANCEMENT_DEFENSE,
                value = 0.06,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_MOVEMENT_PENALTY,
                baseValue = DEFAULT_MOVEMENT_PENALTY,
                enhancementId = ENHANCEMENT_MOVEMENT,
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Active Defense"),
            "Active Defense",
            -getDoubleParam(PARAM_MOVEMENT_PENALTY, player, DEFAULT_MOVEMENT_PENALTY, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.stopAndCooldown()
        if (pressedTime.toFloat() / getMaxUseTime(player) < 0.5f) {
            player.modifyCooldown { it / 2 }
        }
        player.removeAttributes()
        return UseResult.consume()
    }

    override fun getMaxUseTime(player: PlayerEntity): Int = getIntParam(PARAM_DURATION, player, DEFAULT_DURATION, 0)

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount
    else amount * (1f - getFloatParam(PARAM_DEFENSE_VALUE, player, DEFAULT_DEFENSE_VALUE, 0f, 1f))

    override fun shouldRenderFeature(target: PlayerEntity, clientPlayer: PlayerEntity): Boolean =
        target.isUsing() && !target.isUsing(Skills.ABSOLUTE_DEFENSE)

    companion object {

        // Default Values
        private const val DEFAULT_DURATION = 5 * 20
        private const val DEFAULT_DEFENSE_VALUE = 0.2f
        private const val DEFAULT_MOVEMENT_PENALTY = 0.5

        // Parameter Names
        private const val PARAM_DURATION = "duration"  // 持续时间
        private const val PARAM_DEFENSE_VALUE = "defense_value"  // 伤害减免值
        private const val PARAM_MOVEMENT_PENALTY = "movement_penalty"  // 移动速度惩罚

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_DEFENSE = "defense"  // 对应伤害减免
        private const val ENHANCEMENT_MOVEMENT = "movement"  // 对应移动速度
    }
}