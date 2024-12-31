package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.particle.*
import net.minecraft.server.network.*

class ChargedDashSkill : LongPressSkill(
    Settings(
        id = "charged_dash",
        types = listOf(SkillType.MOVEMENT),
        cooldown = 15,
        rarity = SkillRarity.SUPERB
    )
), AttributeTrigger {

    override val timeParamName: String = "charge_time"

    init {
        addParameter(
            name = timeParamName,
            baseValue = 3 * 20,
            enhancementId = "time",
            value = -0.16,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "charge_slowdown",
            baseValue = 0.2,
            enhancementId = "slowdown_reduction",
            value = -0.2,
            operation = Enhancement.Operation.MULTIPLY_TOTAL,
            maxLevel = 5,
            descArg = Enhancement.ArgFormatters.INT_PERCENT
        )

        addParameter(
            name = "velocity_multiplier",
            baseValue = 1.0,
            enhancementId = "multiplier",
            value = 0.2,
            operation = Enhancement.Operation.ADDITION,
            maxLevel = 5
        )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Charged Dash Charging"),
            "Charged Dash Charging",
            -getDoubleParam("charge_slowdown", player, 0.2, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.run {
            stopUsing()
            removeAttributes()
            val multiplier = getDoubleParam("velocity_multiplier", player, 1.0)
            velocity =
                rotationVector.normalize().multiply(2.0 * pressedTime / getPersistTime(player) * multiplier)
            updateVelocity()
            spawnParticles(ParticleTypes.CLOUD, false, pos, 10, 0.5, 0.0, 0.5, 0.1)
        }
        return UseResult.success()
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)
}