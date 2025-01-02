package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.client.*
import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.init.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.client.util.*
import net.minecraft.entity.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

abstract class ReflectionSkill(
    settings: Settings,
    val duration: Int,
    val damageMultiplier: Float = 1.0f,
    private val baseChance: Float?
) : Skill(
    settings
), DamageTrigger, ReflectionTrigger, UsingRenderTrigger {

    override val defaultTime: Int = duration

    init {
        if (!this.settings.types.contains(SkillType.DEFENSE)) {
            this.settings.addTypeToTop(SkillType.DEFENSE)
        }

        this.settings.addParameter("reflection_sound", SoundEvents.ITEM_SHIELD_BLOCK)

        addParameter(
            name = timeParamName,
            baseValue = duration,
            enhancementId = "time",
            value = 0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "damage_multiplier",
            baseValue = damageMultiplier,
            enhancementId = "multiplier",
            value = 0.1f,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        baseChance?.let {
            addParameter(
                name = "reflection_chance",
                baseValue = it,
                enhancementId = "chance",
                value = 0.05f,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatters.INT_PERCENT
            )
        }
    }

    override fun use(user: ServerPlayerEntity): UseResult = startReflecting(user)

    protected fun reflectedFailed(player: ServerPlayerEntity) =
        player.sendMessage(translate("reflection.failed"), true)

    protected fun reflect(
        player: ServerPlayerEntity,
        attacker: LivingEntity?,
        amount: Float,
    ) {
        player.playSoundFromParam("reflection_sound", SoundEvents.ITEM_SHIELD_BLOCK)
        val damage = amount * getFloatParam("damage_multiplier", player, 1.0f, 0f)
        attacker?.damage(player.damageSources.thorns(player), damage)?.let {
            player.sendMessage(
                translate("reflection.${if (it) "success" else "failed"}"),
                true
            )
        }
    }

    protected fun ServerPlayerEntity.reflect(
        attacker: LivingEntity?,
        amount: Float,
    ): Boolean {
        val chance = getFloatParam("reflection_chance", this, baseChance, 0f, 1f)
        return if (random.nextFloat() < chance) {
            reflect(this, attacker, amount)
            true
        } else {
            reflectedFailed(this)
            false
        }
    }

    override fun getRenderModel(target: PlayerEntity, clientPlayer: PlayerEntity): ModelIdentifier =
        Skills.ABSOLUTE_DEFENSE.modelId
}