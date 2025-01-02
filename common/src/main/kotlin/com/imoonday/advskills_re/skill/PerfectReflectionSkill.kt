package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.*
import net.minecraft.entity.damage.*
import net.minecraft.server.network.*
import net.minecraft.sound.*

class PerfectReflectionSkill : ReflectionSkill(
    Settings(
        id = "perfect_reflection",
        cooldown = 5,
        rarity = SkillRarity.EPIC
    ),
    duration = 2,
    damageMultiplier = 1.5f,
    baseChance = null
) {

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter(
                name = "healing_amount_multiplier",
                baseValue = 0.1f,
                enhancementId = "healing_multiplier",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "power",
                baseValue = 1.5,
                enhancementId = "power",
                value = 0.1,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
        super.initDefaultSettings(settings)
    }

    override fun ignoreDamage(
        amount: Float,
        source: DamageSource,
        player: ServerPlayerEntity,
        attacker: Entity?,
    ): Boolean {
        if (!player.isUsing()) return false
        val time = getStartTime(player)?.let {
            System.currentTimeMillis() - it
        }
        player.stopUsing()
        player.stopCooling()
        player.playSoundFromParam("reflection_sound", SoundEvents.ITEM_SHIELD_BLOCK)

        val heal = amount * getFloatParam("healing_amount_multiplier", player, 0.1f, 0f)
        player.heal(heal)

        player.sendMessage(message("success", time?.let { " ${it / 1000.0}s" } ?: ""), true)
        attacker?.run {
            val damage = amount * getFloatParam("damage_multiplier", player, 1.5f, 0f)
            damage(player.damageSources.thorns(player), damage)

            val power = getDoubleParam("power", player, 1.5)
            velocity = pos.subtract(player.pos).normalize().multiply(power, 0.0, power).add(0.0, 0.5, 0.0)
            velocityDirty = true
            (this as? ServerPlayerEntity)?.updateVelocity()
        }
        return true
    }
}