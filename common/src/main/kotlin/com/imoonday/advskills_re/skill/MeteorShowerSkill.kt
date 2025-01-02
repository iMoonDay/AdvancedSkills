package com.imoonday.advskills_re.skill

import com.imoonday.advskills_re.component.*
import com.imoonday.advskills_re.entity.*
import com.imoonday.advskills_re.skill.enums.*
import com.imoonday.advskills_re.skill.trigger.*
import com.imoonday.advskills_re.skill.trigger.client.render.*
import com.imoonday.advskills_re.util.*
import net.minecraft.entity.attribute.*
import net.minecraft.entity.player.*
import net.minecraft.server.network.*
import net.minecraft.util.math.*

class MeteorShowerSkill : LongPressSkill(
    Settings(
        id = "meteor_shower",
        types = listOf(SkillType.ATTACK, SkillType.DESTRUCTION),
        cooldown = 120,
        rarity = SkillRarity.MYTHIC
    )
), AttributeTrigger, UsingRenderTrigger, DangerTrigger {

    override val timeParamName: String = AutoStopTrigger.CHARGE_TIME

    override fun initDefaultSettings(settings: Settings) {
        settings
            .addParameter("min_summon_amount", 5)
            .addParameter("max_summon_amount", 10)
            .addParameter(
                name = AutoStopTrigger.CHARGE_TIME,
                baseValue = 10 * 20,
                enhancementId = "time",
                value = -0.16,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "charge_slowdown",
                baseValue = 0.5,
                enhancementId = "slowdown_reduction",
                value = -0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "extra_summon_amount",
                baseValue = 0,
                enhancementId = "amount",
                value = 2,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT
            ).addParameter(
                name = "range",
                baseValue = 10.0,
                enhancementId = "range",
                value = 2.0,
                operation = Enhancement.Operation.ADDITION,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.FLOAT
            ).addParameter(
                name = "radius_multiplier",
                baseValue = 1.0f,
                enhancementId = "power",
                value = 0.2f,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            ).addParameter(
                name = "velocity_multiplier",
                baseValue = 1.0,
                enhancementId = "velocity",
                value = 0.2,
                operation = Enhancement.Operation.MULTIPLY_TOTAL,
                maxLevel = 5,
                descArg = Enhancement.ArgFormatter.INT_PERCENT
            )
    }

    override fun getAttributes(player: PlayerEntity): Map<EntityAttribute, EntityAttributeModifier> = mapOf(
        EntityAttributes.GENERIC_MOVEMENT_SPEED to EntityAttributeModifier(
            createUuid("Meteor Shower Charging"),
            "Meteor Shower Charging",
            -getDoubleParam("charge_slowdown", player, 0.5, 0.0, 1.0),
            EntityAttributeModifier.Operation.MULTIPLY_TOTAL
        )
    )

    override fun onPress(player: ServerPlayerEntity): UseResult {
        player.addAttributes()
        return super.onPress(player)
    }

    override fun onRelease(player: ServerPlayerEntity, pressedTime: Int): UseResult {
        player.removeAttributes()
        player.stopUsing()
        if (pressedTime < getPersistTime(player)) {
            player.startCooling(10)
            return UseResult.fail(failedMessage())
        }
        val targetPos = player.raycast(512.0, 0f, false).pos
        val random = player.random
        val minAmount = getIntParam("min_summon_amount", player, 5)
        val maxAmount = getIntParam("max_summon_amount", player, 10, minAmount)
        val extraAmount = getIntParam("extra_summon_amount", player, 0)
        val amount = (minAmount..maxAmount).random() + extraAmount
        val range = getDoubleParam("range", player, 10.0)
        val radiusMultiplier = getFloatParam("radius_multiplier", player, 1.0f)
        val velocityMultiplier = getDoubleParam("velocity_multiplier", player, 1.0)

        for (i in 0 until amount) {
            val x = targetPos.x + random.nextDouble() * range * 2 - range
            val z = targetPos.z + random.nextDouble() * range * 2 - range
            val r = (random.nextFloat() + 0.5f) * radiusMultiplier
            player.world.spawnEntity(
                MeteoriteEntity(player.world, Vec3d(x, player.world.topY + r * 2.0, z), r, player).apply {
                    velocity = Vec3d(
                        random.nextDouble() * 0.2 - 0.1,
                        -2.0 * velocityMultiplier,
                        random.nextDouble() * 0.2 - 0.1
                    )
                }
            )
        }
        return UseResult.success()
    }

    override fun onUnequipped(player: ServerPlayerEntity, slot: SkillSlot): Boolean {
        if (player.isUsing()) player.startCooling(10)
        return true
    }

    override fun postUnequipped(player: ServerPlayerEntity, slot: SkillSlot) =
        super<AttributeTrigger>.postUnequipped(player, slot)
}