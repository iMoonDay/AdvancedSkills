package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.damage.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*

class LastDitchEffortSkill : Skill(
    Settings(
        id = "last_ditch_effort",
        types = listOf(SkillType.PASSIVE),
        cooldown = 180,
        rarity = SkillRarity.SUPERB
    )
), DamageTrigger, AutoStopTrigger, AttackTrigger,
    AttributeTrigger, AutoTrigger, DeathTrigger {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND)
            .addParameter(PARAM_HEALTH_THRESHOLD, DEFAULT_HEALTH_THRESHOLD)
            .addParameter(
                name = PARAM_EFFECT_DURATION,
                baseValue = DEFAULT_EFFECT_DURATION,
                enhancementId = ENHANCEMENT_DURATION,
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_SPEED_BOOST,
                baseValue = DEFAULT_SPEED_BOOST,
                enhancementId = ENHANCEMENT_SPEED,
                value = 0.06,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_DAMAGE_BOOST,
                baseValue = DEFAULT_DAMAGE_BOOST,
                enhancementId = ENHANCEMENT_DAMAGE,
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT,
                genericText = true
            ).addParameter(
                name = PARAM_DAMAGE_REDUCTION,
                baseValue = DEFAULT_DAMAGE_REDUCTION,
                enhancementId = ENHANCEMENT_REDUCTION,
                value = -0.2f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = PARAM_HEAL_BOOST,
                baseValue = DEFAULT_HEAL_BOOST,
                enhancementId = ENHANCEMENT_HEAL_BOOST,
                value = 0.1f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Last Ditch Effort"),
            "Last Ditch Effort",
            getDoubleParam(PARAM_SPEED_BOOST, player, DEFAULT_SPEED_BOOST, 0.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) {
        super<AutoStopTrigger>.postUnequipped(player, slot)
        super<AttributeTrigger>.postUnequipped(player, slot)
    }

    override fun use(user: ServerPlayerEntity): UseResult = UseResult.passive(name)

    override fun onAttack(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        target: LivingEntity,
    ): Float = if (!player.isUsing()) amount
    else (amount + 1) * getFloatParam(PARAM_DAMAGE_BOOST, player, DEFAULT_DAMAGE_BOOST)

    override fun shouldStart(player: ServerPlayerEntity): Boolean =
        if (player.isReady() && !player.isDead) {
            val threshold = getFloatParam(PARAM_HEALTH_THRESHOLD, player, DEFAULT_HEALTH_THRESHOLD, 0.0f)
            if ((player.health / player.maxHealth) < threshold) {
                val healBoost = getFloatParam(PARAM_HEAL_BOOST, player, DEFAULT_HEAL_BOOST, 0.0f)
                player.health = player.maxHealth * (threshold + healBoost)
                player.playSoundFromParam(PARAM_HEAL_SOUND, DEFAULT_HEAL_SOUND.get())
                player.addAttributes()
                true
            } else false
        } else false

    override fun onDamaged(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
    ): Float = if (!player.isUsing()) amount
    else amount + getFloatParam(PARAM_DAMAGE_REDUCTION, player, DEFAULT_DAMAGE_REDUCTION)

    override fun getMaxUseTime(player: PlayerEntity): Int =
        getIntParam(PARAM_EFFECT_DURATION, player, DEFAULT_EFFECT_DURATION, 0)

    override fun onStop(player: ServerPlayerEntity) {
        player.startCooling()
        player.removeAttributes()
        super.onStop(player)
    }

    override fun onDeath(player: ServerPlayerEntity, source: DamageSource) {
        super.onDeath(player, source)
        if (player.isUsing()) {
            player.startCooling()
        }
    }

    companion object {

        // Default Values
        private const val DEFAULT_EFFECT_DURATION = 15 * 20
        private const val DEFAULT_SPEED_BOOST = 0.4
        private const val DEFAULT_DAMAGE_BOOST = 1.0f
        private const val DEFAULT_HEAL_RATIO = 0.5f
        private const val DEFAULT_DAMAGE_REDUCTION = 1.0f
        private const val DEFAULT_HEAL_BOOST = 0.2f
        private const val DEFAULT_HEALTH_THRESHOLD = 0.3f
        private val DEFAULT_HEAL_SOUND = ModSounds.HEAL

        // Parameter Names
        private const val PARAM_HEAL_SOUND = "heal_sound"  // 治疗音效
        private const val PARAM_HEALTH_THRESHOLD = "health_threshold"  // 生命阈值
        private const val PARAM_EFFECT_DURATION = "effect_duration"  // 效果持续时间
        private const val PARAM_SPEED_BOOST = "speed_boost"  // 速度提升
        private const val PARAM_DAMAGE_BOOST = "damage_boost"  // 伤害提升
        private const val PARAM_DAMAGE_REDUCTION = "damage_reduction"  // 伤害减免
        private const val PARAM_HEAL_BOOST = "heal_boost"  // 治疗提升

        // Enhancement IDs
        private const val ENHANCEMENT_DURATION = "duration"  // 对应持续时间
        private const val ENHANCEMENT_SPEED = "speed"  // 对应速度提升
        private const val ENHANCEMENT_DAMAGE = "damage"  // 对应伤害提升
        private const val ENHANCEMENT_REDUCTION = "reduction"  // 对应伤害减免
        private const val ENHANCEMENT_HEAL_BOOST = "heal_boost"  // 对应治疗提升
    }
}